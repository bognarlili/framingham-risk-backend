package hu.framingham.risk.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

/**
 * How access tokens from Keycloak are checked.
 *
 * @param jwkSetUri  where the realm's public signing keys are fetched from
 * @param issuerPath required path of the token's issuer; its host varies with the public URL
 * @param audience   value the token's {@code aud} claim must contain
 */
@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(@NotNull URI jwkSetUri, @NotBlank String issuerPath, @NotBlank String audience) {}
