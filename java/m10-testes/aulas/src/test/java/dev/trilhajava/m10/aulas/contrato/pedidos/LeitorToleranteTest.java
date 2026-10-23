package dev.trilhajava.m10.aulas.contrato.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * O provedor evoluiu: acrescentou campos que o contrato não cita. Uma mudança COMPATÍVEL,
 * desde que o consumidor seja um leitor tolerante.
 */
class LeitorToleranteTest {

    @Test
    void camposNovosDoProvedorSaoIgnorados() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new ClienteDeFrete(builder, "http://frete.local");

        servidor.expect(requestTo("http://frete.local/fretes/cotacoes"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "valor": "23.90",
                          "prazoEmDiasUteis": 3,
                          "modalidade": "EXPRESSA",
                          "transportadora": "Rápido Log",
                          "rastreavel": true,
                          "janelaDeEntrega": {"inicio": "08:00", "fim": "18:00"}
                        }""", MediaType.APPLICATION_JSON));

        CotacaoDeFrete cotacao = cliente.cotar("01310-100", 1200);

        assertThat(cotacao.valor()).isEqualByComparingTo("23.90");
        assertThat(cotacao.prazoEmDiasUteis()).isEqualTo(3);
        servidor.verify();
    }
}
