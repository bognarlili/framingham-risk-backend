"""Converts the trained scikit-learn pipelines to ONNX and checks they give the same results.

Besides the probabilities, each ONNX model gets a "contributions" output: coefficient x
preprocessed value for every model column (log-odds). The metadata "contributionFeatures"
names the original feature of each column (the one-hot education columns share one).

Usage: python convert.py <output dir>
"""

import json
import sys
from pathlib import Path

import joblib
import numpy as np
import onnxruntime as ort
import pandas as pd
from onnx import TensorProto, helper, numpy_helper
from skl2onnx import to_onnx
from skl2onnx.common.data_types import FloatTensorType
from sklearn.linear_model import LogisticRegression

HERE = Path(__file__).resolve().parent
OUTPUT_DIR = Path(sys.argv[1]) if len(sys.argv) > 1 else HERE / "out"

# modelKey (as used by the backend) -> trained pipeline.
MODELS = {
    "logistic-regression-full": "lr_hangolt_teljes.joblib",
    "logistic-regression-reduced": "lr_hangolt_csokkentett.joblib",
}

# ONNX runs in float32, so allow a small difference from sklearn's float64 result.
TOLERANCE = 1e-5
CONTRIBUTION_TOLERANCE = 1e-4  # log-odds values are larger than probabilities

# Plausible value ranges (lab values in mg/dL) for the random comparison rows.
RANGES = {
    "male": (0, 1), "age": (30, 70), "education": (1, 4), "currentSmoker": (0, 1),
    "cigsPerDay": (0, 60), "BPMeds": (0, 1), "prevalentStroke": (0, 1), "prevalentHyp": (0, 1),
    "diabetes": (0, 1), "totChol": (110, 600), "sysBP": (80, 250), "diaBP": (50, 140),
    "BMI": (15, 55), "heartRate": (40, 140), "glucose": (40, 400),
}
INTEGER_FEATURES = {"male", "age", "education", "currentSmoker", "cigsPerDay", "BPMeds",
                    "prevalentStroke", "prevalentHyp", "diabetes"}


def random_rows(columns, count, seed=42):
    rng = np.random.default_rng(seed)
    data = {}
    for name in columns:
        low, high = RANGES[name]
        values = rng.uniform(low, high, count)
        data[name] = np.round(values) if name in INTEGER_FEATURES else np.round(values, 1)
    return pd.DataFrame(data, columns=columns)


def run(session, frame):
    # One [n, 1] float input per feature, named like the dataset column.
    inputs = {name: frame[[name]].to_numpy(np.float32) for name in frame.columns}
    return dict(zip([o.name for o in session.get_outputs()], session.run(None, inputs)))


def contribution_features(pipeline):
    """The original feature behind each column the preprocessing outputs."""
    preprocessing = pipeline.named_steps["preprocessing"]
    owners = [None] * len(preprocessing.get_feature_names_out())
    for name, _, columns in preprocessing.transformers_:
        part = preprocessing.output_indices_[name]
        width = part.stop - part.start
        if width == 0:
            continue
        if width == len(columns):
            owners[part] = list(columns)
        else:  # one-hot: several output columns for a single input feature
            assert len(columns) == 1, f"{name}: cannot attribute {width} columns to {columns}"
            owners[part] = [columns[0]] * width
    return owners


def add_contributions(onnx_model, pipeline):
    """Adds output "contributions" = preprocessed model input x coefficients."""
    graph = onnx_model.graph
    classifier = next(node for node in graph.node if node.op_type == "LinearClassifier")
    coefficients = pipeline.named_steps["model"].coef_[0].astype(np.float32).reshape(1, -1)
    graph.initializer.append(numpy_helper.from_array(coefficients, "contribution_coefficients"))
    graph.node.append(helper.make_node("Mul", [classifier.input[0], "contribution_coefficients"],
                                       ["contributions"]))
    graph.output.append(helper.make_tensor_value_info("contributions", TensorProto.FLOAT,
                                                      [None, coefficients.shape[1]]))


def check(model_key, name, expected, actual, tolerance):
    difference = np.abs(expected - actual).max()
    print(f"{model_key}: {name} max difference on 2000 random rows = {difference:.2e}")
    if difference > tolerance:
        sys.exit(f"{model_key}: ONNX {name} differ from sklearn")


def convert(model_key, file_name):
    pipeline = joblib.load(HERE / "models" / file_name)
    columns = list(pipeline.feature_names_in_)

    onnx_model = to_onnx(
        pipeline,
        initial_types=[(name, FloatTensorType([None, 1])) for name in columns],
        options={LogisticRegression: {"zipmap": False}},  # plain probability tensor
    )
    add_contributions(onnx_model, pipeline)
    onnx_model.doc_string = f"{model_key} converted from {file_name}"
    metadata = {"modelKey": model_key, "modelVersion": Path(file_name).stem,
                "features": ",".join(columns),
                "contributionFeatures": ",".join(contribution_features(pipeline))}
    for key, value in metadata.items():
        entry = onnx_model.metadata_props.add()
        entry.key, entry.value = key, value

    session = ort.InferenceSession(onnx_model.SerializeToString(), providers=["CPUExecutionProvider"])
    frame = random_rows(columns, 2000)
    outputs = run(session, frame)
    print(f"{model_key}: {len(columns)} features")
    check(model_key, "probabilities", pipeline.predict_proba(frame)[:, 1],
          outputs["probabilities"][:, 1], TOLERANCE)
    model = pipeline.named_steps["model"]
    check(model_key, "contributions", pipeline[:-1].transform(frame) * model.coef_[0],
          outputs["contributions"], CONTRIBUTION_TOLERANCE)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    (OUTPUT_DIR / f"{model_key}.onnx").write_bytes(onnx_model.SerializeToString())
    return session


def check_example(session):
    example = json.loads((HERE / "examples" / "lr_teljes_pelda.json").read_text(encoding="utf-8"))
    probability = float(run(session, pd.DataFrame([example["input"]]))["probabilities"][0, 1])
    print(f"example: onnx = {probability:.7f}, expected = {example['expected_probability']:.7f}")
    if abs(probability - example["expected_probability"]) > TOLERANCE:
        sys.exit("example: ONNX output differs from the expected probability")


sessions = {key: convert(key, file) for key, file in MODELS.items()}
check_example(sessions["logistic-regression-full"])
