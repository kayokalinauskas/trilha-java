package dev.trilhajava.m11.aulas.senhas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

class PwnedPasswordsPorFaixaTest {

    // SHA-1("password") = 5BAA6 1E4C9B93F3F0682250B6CF8331B7EE68FD8
    private static final String RESPOSTA = """
            1D2DA4053E34E76F6576ED1DA63134B5E2A:2
            1E4C9B93F3F0682250B6CF8331B7EE68FD8:52256179
            1E7C6BA67F8C1D1C0A2D86E2E1DA8BC0C3D:0
            """;

    private final List<String> prefixosEnviados = new ArrayList<>();

    private PwnedPasswordsPorFaixa comResposta(String corpo) {
        return new PwnedPasswordsPorFaixa(prefixo -> {
            prefixosEnviados.add(prefixo);
            return corpo;
        });
    }

    @Test
    @DisplayName("Só os 5 primeiros caracteres do SHA-1 saem do servidor")
    void kAnonimato() {
        comResposta(RESPOSTA).vazada("password");

        assertThat(prefixosEnviados).containsExactly("5BAA6");
    }

    @Test
    @DisplayName("Sufixo presente com contagem maior que zero: vazada")
    void vazada() {
        assertThat(comResposta(RESPOSTA).vazada("password")).isTrue();
    }

    @Test
    @DisplayName("Sufixo ausente: não vazada")
    void naoVazada() {
        assertThat(comResposta(RESPOSTA).vazada("cachorro late no quintal")).isFalse();
    }

    @Test
    @DisplayName("Linha de preenchimento (contagem 0) não conta")
    void preenchimento() {
        String soPreenchimento = "1E4C9B93F3F0682250B6CF8331B7EE68FD8:0\r\n";

        assertThat(comResposta(soPreenchimento).vazada("password")).isFalse();
    }

    @Test
    @DisplayName("API fora do ar: fail-open, a senha é aceita")
    void apiForaDoAr() {
        var indisponivel = new PwnedPasswordsPorFaixa(prefixo -> {
            throw new ResourceAccessException("I/O error on GET request");
        });

        assertThat(indisponivel.vazada("password")).isFalse();
    }
}
