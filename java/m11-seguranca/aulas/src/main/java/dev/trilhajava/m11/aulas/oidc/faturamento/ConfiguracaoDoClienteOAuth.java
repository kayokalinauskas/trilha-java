package dev.trilhajava.m11.aulas.oidc.faturamento;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ConfiguracaoDoClienteOAuth {

    /**
     * Fora de uma requisição HTTP de usuário (um job, um consumidor de fila), o gerenciador
     * certo é o baseado em OAuth2AuthorizedClientService: ele guarda o token obtido e o
     * reaproveita até perto de expirar.
     */
    @Bean
    OAuth2AuthorizedClientManager gerenciadorDeClientesAutorizados(
            ClientRegistrationRepository registros, OAuth2AuthorizedClientService clientesAutorizados) {
        var provedor = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        var gerenciador = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registros, clientesAutorizados);
        gerenciador.setAuthorizedClientProvider(provedor);
        return gerenciador;
    }

    @Bean
    ClienteDaApiDePedidos clienteDaApiDePedidos(
            OAuth2AuthorizedClientManager gerenciador,
            @Value("${faturamento.api-de-pedidos.url}") String urlDaApi) {
        var interceptor = new OAuth2ClientHttpRequestInterceptor(gerenciador);
        // Toda chamada deste RestClient usa o registro "api-de-pedidos" (faturamento.yml).
        interceptor.setClientRegistrationIdResolver(requisicao -> "api-de-pedidos");
        RestClient restClient = RestClient.builder()
                .baseUrl(urlDaApi)
                .requestInterceptor(interceptor)
                .build();
        return new ClienteDaApiDePedidos(restClient);
    }
}
