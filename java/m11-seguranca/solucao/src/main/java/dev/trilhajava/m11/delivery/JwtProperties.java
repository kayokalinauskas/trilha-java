package dev.trilhajava.m11.delivery;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * As propriedades {@code delivery.jwt.*} de {@code delivery.properties}: o segredo do HS256, o emissor
 * ({@code iss}) e a validade dos tokens. Recusa, na subida, um segredo com menos de 32 bytes.
 */
@ConfigurationProperties("delivery.jwt")
public record JwtProperties(String segredo, String emissor, Duration validade) {

    public JwtProperties {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("delivery.jwt.segredo precisa de pelo menos 32 bytes para HS256");
        }
    }
}
