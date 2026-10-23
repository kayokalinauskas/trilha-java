package dev.trilhajava.m11.aulas.jwt;

import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.claimsValidos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/** Cada "kid" desconhecido faz o decoder buscar o JWKS de novo. Quantas buscas um atacante consegue provocar? */
class JwksComLimiteTest {

    final AutorizadorDeTeste autorizador = new AutorizadorDeTeste(AutorizadorDeTeste.novaChave("chave-2026-09"));

    @AfterEach
    void desligar() {
        autorizador.close();
    }

    @Test
    void decoderPadraoBuscaOJwksACadaKidDesconhecido() {
        JwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(autorizador.jwksUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();

        decoder.decode(autorizador.emitir(claimsValidos().build()));
        recusarCincoKidsInventados(decoder);

        assertThat(autorizador.consultasAoJwks()).isEqualTo(6);
    }

    @Test
    void jwkSourceComLimiteDeFrequencia() throws Exception {
        JWKSource<SecurityContext> chaves = JWKSourceBuilder
                .<SecurityContext>create(URI.create(autorizador.jwksUri()).toURL())
                .cache(5 * 60_000, 15_000)      // guarda o JWKS por 5 min; espera até 15 s por uma atualização
                .rateLimited(30_000)            // no máximo uma busca a cada 30 s
                .build();
        JwtDecoder decoder = NimbusJwtDecoder.withJwkSource(chaves)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();

        decoder.decode(autorizador.emitir(claimsValidos().build()));
        recusarCincoKidsInventados(decoder);

        assertThat(autorizador.consultasAoJwks()).isLessThanOrEqualTo(2);
    }

    private void recusarCincoKidsInventados(JwtDecoder decoder) {
        for (int i = 1; i <= 5; i++) {
            String token = AutorizadorDeTeste.emitir(AutorizadorDeTeste.novaChave("kid-inventado-" + i),
                    claimsValidos().build());
            assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
        }
    }
}
