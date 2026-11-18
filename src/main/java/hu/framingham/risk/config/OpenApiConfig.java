package hu.framingham.risk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI customOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Framingham CHD Risk API")
                .version("0.1.0")
                .description("Educational demonstration only. Not validated for clinical use. "
                        + "No ML model is currently configured."));
    }
}
