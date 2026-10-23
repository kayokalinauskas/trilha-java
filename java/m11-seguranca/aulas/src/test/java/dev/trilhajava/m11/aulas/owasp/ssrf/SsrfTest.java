package dev.trilhajava.m11.aulas.owasp.ssrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SsrfTest {

    // Um "DNS" de mentira: o teste decide para onde cada nome aponta, sem rede.
    static final Map<String, String> ZONA = Map.of(
            "api.parceiro.com.br", "203.0.113.10",
            "webhooks.parceiro.com.br", "203.0.113.11",
            "interno.parceiro.com.br", "10.0.0.5",          // registro DNS apontando para a rede interna
            "metadados.parceiro.com.br", "169.254.169.254"); // metadados da nuvem

    static final ResolvedorDeNomes DNS_DE_TESTE = host -> {
        String ip = ZONA.get(host);
        if (ip == null) {
            throw new UnknownHostException(host);
        }
        return new InetAddress[] {InetAddress.getByName(ip)}; // IP literal: não consulta DNS
    };

    final ValidadorDeDestino validador = new ValidadorDeDestino(Set.of("*.parceiro.com.br"), DNS_DE_TESTE);

    @ParameterizedTest
    @ValueSource(strings = {
            "https://parceiro.com.br.atacante.net/webhook",
            "https://api.parceiro.com.br@169.254.169.254/latest/meta-data/",
            "https://169.254.169.254/latest/meta-data/?parceiro.com.br"})
    void validacaoPorTextoAceitaDestinosPerigosos(String url) {
        assertThat(ValidacaoIngenuaDeUrl.permitida(url)).isTrue();
    }

    @Test
    void urlLegitimaPassaEDevolveOIpConferido() {
        var destino = validador.validar("https://api.parceiro.com.br/webhooks/pedidos");

        assertThat(destino.uri().getHost()).isEqualTo("api.parceiro.com.br");
        assertThat(destino.endereco().getHostAddress()).isEqualTo("203.0.113.10");
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(delimiter = '|', textBlock = """
            https://parceiro.com.br.atacante.net/webhook                    | domínio fora da lista: parceiro.com.br.atacante.net
            https://api.parceiro.com.br@169.254.169.254/latest/meta-data/   | URL com usuário/senha
            https://169.254.169.254/latest/meta-data/?parceiro.com.br       | domínio fora da lista: 169.254.169.254
            http://api.parceiro.com.br/webhooks                             | só https é aceito
            file:///etc/passwd                                              | só https é aceito
            https://api.parceiro.com.br:8080/webhooks                       | porta não permitida: 8080
            https://interno.parceiro.com.br/webhooks                        | interno.parceiro.com.br resolve para endereço interno 10.0.0.5
            https://metadados.parceiro.com.br/latest                        | metadados.parceiro.com.br resolve para endereço interno 169.254.169.254
            https://sumiu.parceiro.com.br/webhooks                          | domínio não resolve: sumiu.parceiro.com.br
            """)
    void validadorRecusaCadaTruque(String url, String motivo) {
        assertThatThrownBy(() -> validador.validar(url))
                .isInstanceOf(DestinoRecusadoException.class)
                .hasMessage(motivo);
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "0.0.0.0", "10.1.2.3", "172.16.0.1", "192.168.0.10", "169.254.169.254",
            "100.64.0.1", "::1", "fe80::1", "fd00::1", "::ffff:169.254.169.254"})
    void faixasInternasNaoSaoPublicas(String ip) throws UnknownHostException {
        assertThat(ValidadorDeDestino.publico(InetAddress.getByName(ip))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"203.0.113.10", "8.8.8.8", "2001:4860:4860::8888"})
    void enderecosPublicos(String ip) throws UnknownHostException {
        assertThat(ValidadorDeDestino.publico(InetAddress.getByName(ip))).isTrue();
    }
}
