package dev.trilhajava.m10.aulas.contrato.pedidos;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Adaptador do serviço de PEDIDOS para a API do serviço de frete: o lado CONSUMIDOR do contrato.
 * O resto do serviço de pedidos só conhece este método e os tipos {@link CotacaoDeFrete} e
 * {@link FreteIndisponivelException}; HTTP, JSON e códigos de status ficam aqui dentro.
 */
public class ClienteDeFrete {

    private final RestClient http;

    public ClienteDeFrete(RestClient.Builder builder, String urlBase) {
        this.http = builder.baseUrl(urlBase).build();
    }

    public CotacaoDeFrete cotar(String cepDestino, int pesoEmGramas) {
        return http.post()
                .uri("/fretes/cotacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PedidoDeCotacao(cepDestino, pesoEmGramas))
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.UNPROCESSABLE_CONTENT),
                        (requisicao, resposta) -> {
                            throw new FreteIndisponivelException(cepDestino);
                        })
                .body(CotacaoDeFrete.class);
    }

    /** O corpo que o consumidor envia: uma cópia local, não a classe do provedor. */
    record PedidoDeCotacao(String cepDestino, int pesoEmGramas) {
    }
}
