package dev.trilhajava.fintech.infra;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** fintech.jwt.* — validado na subida: com um segredo fraco, a aplicação nem inicia. */
@ConfigurationProperties("fintech.jwt")
public record JwtProperties(String segredo, String emissor, Duration validade) {

    public JwtProperties {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("fintech.jwt.segredo precisa de pelo menos 32 bytes (256 bits) para HS256");
        }
        if (emissor == null || emissor.isBlank()) {
            throw new IllegalStateException("fintech.jwt.emissor é obrigatório");
        }
        if (validade == null || validade.compareTo(Duration.ofMinutes(1)) < 0 || validade.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalStateException("fintech.jwt.validade deve estar entre 1 min e 1 h");
        }
    }
}
