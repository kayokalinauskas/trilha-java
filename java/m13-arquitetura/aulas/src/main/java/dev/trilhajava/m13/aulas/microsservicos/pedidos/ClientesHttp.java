package dev.trilhajava.m13.aulas.microsservicos.pedidos;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** Monta o cliente HTTP do estoque: endereço, timeouts e versão da API num lugar só. */
public final class ClientesHttp {

    private ClientesHttp() {
    }

    public static EstoqueCliente estoque(String urlBase, Duration timeoutDeLeitura) {
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))           // tempo para abrir a conexão
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(timeoutDeLeitura);                 // tempo para a resposta chegar

        RestClient restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .apiVersionInserter(ApiVersionInserter.useHeader("API-Version"))
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(EstoqueCliente.class);
    }
}
