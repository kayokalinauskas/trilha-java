package dev.trilhajava.m08.cambio;

import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class CambioConfig {

    /**
     * O cliente com timeouts explícitos. Sem timeout, uma API externa lenta prende as threads
     * da sua aplicação até todas se esgotarem — a falha de um fornecedor vira a SUA queda.
     */
    @Bean
    CotacaoClient cotacaoClient(RestClient.Builder builder, CambioProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());

        RestClient restClient = builder
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(CotacaoClient.class);
    }

    /** O "agora" vira uma dependência: nos testes, dá para avançar o relógio sem esperar. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
