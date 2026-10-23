package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PKCE (RFC 7636)")
class PkceTest {

    @Test
    @DisplayName("S256 do verifier do Apêndice B da RFC 7636 dá o challenge publicado na RFC")
    void exemploDaRfc() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

        assertThat(Pkce.challengeS256(verifier))
                .isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    @DisplayName("o verifier gerado tem 43 caracteres do alfabeto Base64URL, sem padding")
    void verifierGerado() {
        String verifier = Pkce.novoVerifier(new SecureRandom());

        assertThat(verifier)
                .hasSize(43)
                .matches("[A-Za-z0-9_-]+");
    }

    @Test
    @DisplayName("cada login usa um verifier novo, e portanto um challenge novo")
    void verifiersDiferentes() {
        var aleatorio = new SecureRandom();
        String primeiro = Pkce.novoVerifier(aleatorio);
        String segundo = Pkce.novoVerifier(aleatorio);

        assertThat(primeiro).isNotEqualTo(segundo);
        assertThat(Pkce.challengeS256(primeiro)).isNotEqualTo(Pkce.challengeS256(segundo));
    }
}
