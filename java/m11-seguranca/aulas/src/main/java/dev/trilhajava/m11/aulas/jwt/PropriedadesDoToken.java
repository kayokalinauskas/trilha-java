package dev.trilhajava.m11.aulas.jwt;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * O que a API precisa saber para confiar num token.
 *
 * @param emissor             valor exato esperado no claim "iss"
 * @param audiencia           identificador desta API, que precisa estar no claim "aud"
 * @param jwkSetUri           endereço do JWKS: as chaves públicas do emissor
 * @param toleranciaDoRelogio diferença de relógio aceita ao conferir exp e nbf
 */
@ConfigurationProperties("extrato.seguranca")
public record PropriedadesDoToken(String emissor, String audiencia, String jwkSetUri, Duration toleranciaDoRelogio) {

    public PropriedadesDoToken {
        Objects.requireNonNull(emissor, "extrato.seguranca.emissor é obrigatório");
        Objects.requireNonNull(audiencia, "extrato.seguranca.audiencia é obrigatório");
        Objects.requireNonNull(jwkSetUri, "extrato.seguranca.jwk-set-uri é obrigatório");
        if (toleranciaDoRelogio == null || toleranciaDoRelogio.isNegative()
                || toleranciaDoRelogio.compareTo(Duration.ofMinutes(2)) > 0) {
            throw new IllegalStateException("extrato.seguranca.tolerancia-do-relogio deve ficar entre 0 e 2 minutos");
        }
    }
}
