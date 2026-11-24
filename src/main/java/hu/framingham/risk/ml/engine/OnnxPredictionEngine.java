package hu.framingham.risk.ml.engine;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.framingham.risk.exception.ModelUnavailableException;
import hu.framingham.risk.ml.model.PredictionResult;
import hu.framingham.risk.ml.model.RiskFeatures;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs the scikit-learn pipelines converted to ONNX (see model-conversion/).
 * Every model is loaded once at startup; its key and version come from the file's metadata.
 */
@Slf4j
@Component
public class OnnxPredictionEngine implements PredictionEngine {

    private static final String MODEL_LOCATION = "classpath:models/*.onnx";
    private static final String PROBABILITIES_OUTPUT = "probabilities";
    private static final String CONTRIBUTIONS_OUTPUT = "contributions";
    private static final int POSITIVE_CLASS = 1;
    /** Features keyed by their JSON (dataset column) name; null values are kept. */
    private static final TypeReference<Map<String, Number>> FEATURE_MAP = new TypeReference<>() {};

    /** {@code contributionFeatures}: the input feature behind each column of the contributions output. */
    private record LoadedModel(String version, List<String> contributionFeatures, OrtSession session) {}

    private final ObjectMapper objectMapper;
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private final Map<String, LoadedModel> models = new HashMap<>();

    public OnnxPredictionEngine(ObjectMapper objectMapper) throws IOException, OrtException {
        this.objectMapper = objectMapper;
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MODEL_LOCATION);
        try (OrtSession.SessionOptions options = new OrtSession.SessionOptions()) {
            for (Resource resource : resources) {
                OrtSession session = this.environment.createSession(resource.getContentAsByteArray(), options);
                Map<String, String> metadata = session.getMetadata().getCustomMetadata();
                String key = metadata.get("modelKey");
                List<String> contributionFeatures = List.of(metadata.get("contributionFeatures").split(","));
                this.models.put(key, new LoadedModel(metadata.get("modelVersion"), contributionFeatures, session));
                log.info("Loaded ONNX model {} ({}) with inputs {}", key, metadata.get("modelVersion"),
                         session.getInputNames());
            }
        }
    }

    @Override
    public boolean isAvailable(String modelKey) {
        return this.models.containsKey(modelKey);
    }

    @Override
    public PredictionResult predict(String modelKey, RiskFeatures features) {
        LoadedModel model = this.models.get(modelKey);
        if (model == null) {
            throw new ModelUnavailableException("Unknown model: " + modelKey);
        }
        Map<String, Number> values = this.objectMapper.convertValue(features, FEATURE_MAP);
        Map<String, OnnxTensor> inputs = new HashMap<>();
        try {
            // One [1, 1] float tensor per feature; a missing value becomes NaN for the model's imputer.
            for (String name : model.session().getInputNames()) {
                if (!values.containsKey(name)) {
                    throw new IllegalStateException("Model " + modelKey + " expects unknown feature " + name);
                }
                Number value = values.get(name);
                float input = value == null ? Float.NaN : value.floatValue();
                inputs.put(name, OnnxTensor.createTensor(this.environment, new float[][] {{input}}));
            }
            try (OrtSession.Result result = model.session().run(inputs)) {
                float[][] probabilities = (float[][]) result.get(PROBABILITIES_OUTPUT).orElseThrow().getValue();
                float[][] columns = (float[][]) result.get(CONTRIBUTIONS_OUTPUT).orElseThrow().getValue();
                return new PredictionResult(model.version(), probabilities[0][POSITIVE_CLASS],
                                            contributionsByFeature(model.contributionFeatures(), columns[0]));
            }
        } catch (OrtException exception) {
            throw new IllegalStateException("ONNX inference failed for model " + modelKey, exception);
        } finally {
            inputs.values().forEach(OnnxTensor::close);
        }
    }

    /** Sums the column contributions per input feature (e.g. the one-hot education columns). */
    private static Map<String, Double> contributionsByFeature(List<String> features, float[] columns) {
        Map<String, Double> contributions = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            contributions.merge(features.get(i), (double) columns[i], Double::sum);
        }
        return contributions;
    }

    @PreDestroy
    void close() throws OrtException {
        for (LoadedModel model : this.models.values()) {
            model.session().close();
        }
    }
}
