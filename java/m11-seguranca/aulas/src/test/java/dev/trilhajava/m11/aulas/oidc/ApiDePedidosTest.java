package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m11.aulas.oidc.api.ApiDePedidosApplication;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * A API de pedidos sobe de verdade (porta aleatória) e recebe tokens de verdade, emitidos
 * pelo servidor de autorização para clientes diferentes.
 */
@SpringBootTest(
        classes = ApiDePedidosApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.name=api-de-pedidos")
@DisplayName("API de pedidos como Resource Server")
class ApiDePedidosTest {

    @DynamicPropertySource
    static void keycloak(DynamicPropertyRegistry registro) {
        registro.add("EMISSOR", ServidorDeAutorizacaoDeTeste::emissor); // usado pelo api-de-pedidos.yml
    }

    @LocalServerPort
    int porta;

    private String pedido(String numero) {
        return "http://localhost:" + porta + "/pedidos/" + numero;
    }

    @Test
    @DisplayName("token do faturamento (aud certa e escopo pedidos:ler): 200")
    void faturamentoConsulta() {
        String token = Http.tokenDeServico("servico-de-faturamento", ServidorDeAutorizacaoDeTeste.SEGREDO_FATURAMENTO, "pedidos:ler");

        HttpResponse<String> resposta = Http.getComToken(pedido("PED-2026-0042"), token);

        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.body())
                .contains("\"numero\":\"PED-2026-0042\"")
                .contains("\"chamadoPor\":\"servico-de-faturamento\"");
    }

    @Test
    @DisplayName("sem token: 401 com WWW-Authenticate: Bearer, apontando os metadados do recurso (RFC 9728)")
    void semToken() {
        HttpResponse<String> resposta = Http.get(pedido("PED-2026-0042"));

        assertThat(resposta.statusCode()).isEqualTo(401);
        assertThat(resposta.headers().firstValue("WWW-Authenticate").orElseThrow())
                .isEqualTo("Bearer resource_metadata=\"http://localhost:" + porta + "/.well-known/oauth-protected-resource\"");
    }

    @Test
    @DisplayName("token válido do mesmo emissor, mas emitido para outra audiência: 401")
    void audienciaErrada() {
        String token = Http.tokenDeServico("servico-de-relatorios", ServidorDeAutorizacaoDeTeste.SEGREDO_RELATORIOS, "relatorios:gerar");

        HttpResponse<String> resposta = Http.getComToken(pedido("PED-2026-0042"), token);

        assertThat(resposta.statusCode()).isEqualTo(401);
        assertThat(resposta.headers().firstValue("WWW-Authenticate").orElseThrow())
                .contains("error=\"invalid_token\"")
                .contains("aud");
    }

    @Test
    @DisplayName("audiência certa, mas sem o escopo: 403 (autenticado, não autorizado)")
    void semPapel() {
        String token = Http.tokenDeServico("servico-de-auditoria", ServidorDeAutorizacaoDeTeste.SEGREDO_AUDITORIA, "pedidos:auditar");

        HttpResponse<String> resposta = Http.getComToken(pedido("PED-2026-0042"), token);

        assertThat(resposta.statusCode()).isEqualTo(403);
        assertThat(resposta.headers().firstValue("WWW-Authenticate").orElseThrow())
                .contains("error=\"insufficient_scope\"");
    }

    @Test
    @DisplayName("token adulterado (corpo trocado pelo de outro token, assinatura mantida): 401")
    void tokenAdulterado() {
        String[] relatorios = Http.tokenDeServico(
                "servico-de-relatorios", ServidorDeAutorizacaoDeTeste.SEGREDO_RELATORIOS, "relatorios:gerar").split("\\.");
        String[] faturamento = Http.tokenDeServico(
                "servico-de-faturamento", ServidorDeAutorizacaoDeTeste.SEGREDO_FATURAMENTO, "pedidos:ler").split("\\.");
        // cabeçalho e assinatura de relatórios, corpo (aud e escopo certos) do faturamento
        String adulterado = relatorios[0] + "." + faturamento[1] + "." + relatorios[2];

        HttpResponse<String> resposta = Http.getComToken(pedido("PED-2026-0042"), adulterado);

        assertThat(resposta.statusCode()).isEqualTo(401);
        assertThat(resposta.headers().firstValue("WWW-Authenticate").orElseThrow())
                .contains("error=\"invalid_token\"");
    }
}
