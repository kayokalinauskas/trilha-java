package dev.trilhajava.m11.aulas.owasp.logs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class InjecaoEmLogTest {

    // Um "e-mail" com CRLF e uma linha falsa de sucesso logo depois.
    static final String EMAIL_FORJADO = "ana@exemplo.com\r\nINFO login-ok email=admin@fintech.com ip=10.0.0.1";

    @Test
    void versaoVulneravelDeixaOAtacanteEscreverUmaLinhaNova() {
        var arquivo = new StringBuilder();
        AuditoriaDeLogin.vulneravel(arquivo::append).falhou(EMAIL_FORJADO, "203.0.113.7");

        List<String> linhas = arquivo.toString().lines().toList();
        assertThat(linhas).containsExactly(
                "WARN login-falhou email=ana@exemplo.com",
                "INFO login-ok email=admin@fintech.com ip=10.0.0.1 ip=203.0.113.7");
    }

    @Test
    void versaoSeguraMantemTudoNumaLinhaSo() {
        var arquivo = new StringBuilder();
        AuditoriaDeLogin.segura(arquivo::append).falhou(EMAIL_FORJADO, "203.0.113.7");

        assertThat(arquivo.toString().lines()).containsExactly(
                "WARN login-falhou email=ana@exemplo.com\\r\\nINFO login-ok email=admin@fintech.com ip=10.0.0.1"
                        + " ip=203.0.113.7");
    }

    @Test
    void outrosCaracteresDeControleECorte() {
        assertThat(NeutralizadorDeLog.limpar("a\u0000b\u001b[31mc d")).isEqualTo("a\\u0000b\\u001b[31mc\\u2028d");
        assertThat(NeutralizadorDeLog.limpar("x".repeat(205))).endsWith("…(+5)");
        assertThat(NeutralizadorDeLog.limpar(null)).isEqualTo("null");
    }
}
