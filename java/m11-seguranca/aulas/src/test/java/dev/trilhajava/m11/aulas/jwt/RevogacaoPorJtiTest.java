package dev.trilhajava.m11.aulas.jwt;

import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.claimsValidos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.jwk.RSAKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/** Solução do checkpoint: um validador próprio, testado direto no decoder, sem subir o Spring. */
class RevogacaoPorJtiTest {

    static final RSAKey CHAVE = AutorizadorDeTeste.novaChave("chave-2026-09");

    final Set<String> revogados = ConcurrentHashMap.newKeySet();
    NimbusJwtDecoder decoder;

    @BeforeEach
    void montarDecoder() throws Exception {
        decoder = NimbusJwtDecoder.withPublicKey(CHAVE.toRSAPublicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                new ValidadorDeRevogacao(revogados::contains)));
    }

    @Test
    void tokenNaoRevogadoPassa() {
        String token = AutorizadorDeTeste.emitir(CHAVE, claimsValidos().id("jti-ativo").build());

        assertThat(decoder.decode(token).getId()).isEqualTo("jti-ativo");
    }

    @Test
    void tokenRevogadoNoLogoutERecusado() {
        String token = AutorizadorDeTeste.emitir(CHAVE, claimsValidos().id("jti-do-celular-roubado").build());
        revogados.add("jti-do-celular-roubado");

        assertThatThrownBy(() -> decoder.decode(token))
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("The token has been revoked");
    }

    @Test
    void tokenSemJtiERecusado() {
        JwtClaimsSet semJti = JwtClaimsSet.from(claimsValidos().build()).claims(c -> c.remove("jti")).build();
        String token = AutorizadorDeTeste.emitir(CHAVE, semJti);

        assertThatThrownBy(() -> decoder.decode(token))
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("The jti claim is required");
    }
}
