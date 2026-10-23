package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m11.aulas.oidc.api.ApiDePedidosApplication;
import dev.trilhajava.m11.aulas.oidc.faturamento.ClienteDaApiDePedidos;
import dev.trilhajava.m11.aulas.oidc.faturamento.ConfiguracaoDoClienteOAuth;
import dev.trilhajava.m11.aulas.oidc.faturamento.PedidoParaFaturar;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;

/**
 * Em produção, faturamento e API de pedidos são dois serviços. Aqui os dois rodam no mesmo
 * contexto, para o teste ser simples: o RestClient do faturamento sai pela rede (porta real),
 * pede o token ao servidor de autorização e chama a API, que valida o token como qualquer outro.
 */
@SpringBootTest(
        classes = {ApiDePedidosApplication.class, ConfiguracaoDoClienteOAuth.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "spring.config.name=api-de-pedidos,faturamento")
@DisplayName("Faturamento chama a API de pedidos com client credentials")
class FaturamentoChamaApiTest {

    static final int PORTA = TestSocketUtils.findAvailableTcpPort();

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registro) {
        registro.add("EMISSOR", ServidorDeAutorizacaoDeTeste::emissor);
        registro.add("FATURAMENTO_CLIENT_SECRET", () -> ServidorDeAutorizacaoDeTeste.SEGREDO_FATURAMENTO);
        registro.add("server.port", () -> PORTA);
        registro.add("API_DE_PEDIDOS_URL", () -> "http://localhost:" + PORTA);
    }

    @Autowired
    ClienteDaApiDePedidos api;

    @Autowired
    OAuth2AuthorizedClientService clientesAutorizados;

    @Test
    @DisplayName("o interceptor obtém o token, chama a API e reaproveita o token na chamada seguinte")
    void chamaComToken() {
        PedidoParaFaturar pedido = api.buscar("PED-2026-0042");

        assertThat(pedido.numero()).isEqualTo("PED-2026-0042");
        assertThat(pedido.total()).isEqualByComparingTo(new BigDecimal("259.90"));
        assertThat(pedido.chamadoPor()).isEqualTo("servico-de-faturamento");

        String primeiroToken = tokenGuardado();
        api.buscar("PED-2026-0043");
        assertThat(tokenGuardado()).isEqualTo(primeiroToken); // sem ida extra ao servidor de autorização
    }

    private String tokenGuardado() {
        // Sem usuário logado, o interceptor usa o principal "anonymousUser".
        OAuth2AuthorizedClient cliente = clientesAutorizados.loadAuthorizedClient("api-de-pedidos", "anonymousUser");
        assertThat(cliente).isNotNull();
        return cliente.getAccessToken().getTokenValue();
    }
}
