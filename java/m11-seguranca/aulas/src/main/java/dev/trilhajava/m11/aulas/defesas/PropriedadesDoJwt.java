package dev.trilhajava.m11.aulas.defesas;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * O segredo vem de fora: {@code conta.jwt.segredo=${CONTA_JWT_SEGREDO}} no application.yml, preenchido
 * pelo cofre de segredos na subida. Sem ele, ou curto demais, a aplicação NÃO sobe.
 */
@ConfigurationProperties("conta.jwt")
public record PropriedadesDoJwt(String segredo) {

    public PropriedadesDoJwt {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException(
                    "conta.jwt.segredo não configurado: defina a variável de ambiente CONTA_JWT_SEGREDO");
        }
        if (segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("conta.jwt.segredo precisa de pelo menos 32 bytes para HS256");
        }
    }

    /** Nunca imprima o segredo: nem em log, nem em toString, nem em mensagem de erro. */
    @Override
    public String toString() {
        return "PropriedadesDoJwt[segredo=***]";
    }
}
