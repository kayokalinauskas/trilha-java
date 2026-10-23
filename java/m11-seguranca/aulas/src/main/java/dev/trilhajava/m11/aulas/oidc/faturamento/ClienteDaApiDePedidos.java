package dev.trilhajava.m11.aulas.oidc.faturamento;

import org.springframework.web.client.RestClient;

/** Quem usa esta classe não sabe que existe OAuth: o token entra pelo interceptor do RestClient. */
public class ClienteDaApiDePedidos {

    private final RestClient restClient;

    public ClienteDaApiDePedidos(RestClient restClient) {
        this.restClient = restClient;
    }

    public PedidoParaFaturar buscar(String numero) {
        return restClient.get()
                .uri("/pedidos/{numero}", numero)
                .retrieve()
                .body(PedidoParaFaturar.class);
    }
}
