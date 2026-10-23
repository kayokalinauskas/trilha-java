package dev.trilhajava.m10.aulas.contrato.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.web.client.RestClient;

/**
 * O lado CONSUMIDOR: o ClienteDeFrete real, com RestClient real, fala com um WireMock que o
 * Stub Runner sobe a partir dos stubs gerados dos MESMOS contratos que o provedor verifica.
 */
class ClienteDeFreteContratoTest {

    @RegisterExtension
    static StubRunnerExtension frete = new StubRunnerExtension()
            .downloadStub("dev.trilhajava", "m10-testes-aulas")
            .repoRoot("stubs://" + Path.of("target/stubs").toAbsolutePath().toUri())
            .stubsMode(StubRunnerProperties.StubsMode.REMOTE);

    ClienteDeFrete cliente;

    @BeforeEach
    void criarCliente() {
        String urlDoStub = frete.findStubUrl("m10-testes-aulas").toString();
        cliente = new ClienteDeFrete(RestClient.builder(), urlDoStub);
    }

    @Test
    void cotaOFreteParaUmCepAtendido() {
        CotacaoDeFrete cotacao = cliente.cotar("01310-100", 1200);

        assertThat(cotacao.valor()).isEqualByComparingTo("23.90");
        assertThat(cotacao.prazoEmDiasUteis()).isEqualTo(3);
    }

    @Test
    void cepSemCoberturaViraFreteIndisponivel() {
        assertThatThrownBy(() -> cliente.cotar("69005-040", 800))
                .isInstanceOf(FreteIndisponivelException.class)
                .hasMessageContaining("69005-040");
    }
}
