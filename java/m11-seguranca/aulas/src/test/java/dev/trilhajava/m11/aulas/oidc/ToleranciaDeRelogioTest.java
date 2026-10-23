package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

/**
 * Os relógios do emissor e da API nunca batem perfeitamente. O validador de exp/nbf do
 * Spring Security aceita, por padrão, 60 segundos de diferença (clock skew).
 */
@DisplayName("Tolerância de relógio na validação de exp")
class ToleranciaDeRelogioTest {

    static final Instant AGORA = Instant.parse("2026-10-05T12:00:00Z");

    private static Jwt tokenQueVenceuEm(Instant exp) {
        return Jwt.withTokenValue("token-de-teste")
                .header("alg", "RS256")
                .subject("servico-de-faturamento")
                .issuedAt(exp.minus(Duration.ofMinutes(5)))
                .expiresAt(exp)
                .build();
    }

    private static JwtTimestampValidator validadorPadrao() {
        var validador = new JwtTimestampValidator(); // tolerância padrão: 60 s
        validador.setClock(Clock.fixed(AGORA, ZoneOffset.UTC));
        return validador;
    }

    @Test
    @DisplayName("vencido há 30 s: ainda passa (dentro da tolerância)")
    void dentroDaTolerancia() {
        assertThat(validadorPadrao().validate(tokenQueVenceuEm(AGORA.minusSeconds(30))).hasErrors()).isFalse();
    }

    @Test
    @DisplayName("vencido há 2 min: invalid_token, Jwt expired at ...")
    void foraDaTolerancia() {
        var resultado = validadorPadrao().validate(tokenQueVenceuEm(AGORA.minusSeconds(120)));

        assertThat(resultado.getErrors())
                .extracting(OAuth2Error::getErrorCode, OAuth2Error::getDescription)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("invalid_token", "Jwt expired at 2026-10-05T11:58:00Z"));
    }

    @Test
    @DisplayName("com tolerância de 10 s, o mesmo token de 30 s já é recusado")
    void toleranciaMenor() {
        var validador = new JwtTimestampValidator(Duration.ofSeconds(10));
        validador.setClock(Clock.fixed(AGORA, ZoneOffset.UTC));

        assertThat(validador.validate(tokenQueVenceuEm(AGORA.minusSeconds(30))).hasErrors()).isTrue();
    }
}
