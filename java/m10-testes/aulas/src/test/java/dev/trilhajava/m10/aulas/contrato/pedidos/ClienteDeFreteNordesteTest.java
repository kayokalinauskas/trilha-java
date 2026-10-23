package dev.trilhajava.m10.aulas.contrato.pedidos;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.web.client.RestClient;

/** Solução do checkpoint da aula 10.07: o consumidor depende de uma nova interação, a do Nordeste. */
class ClienteDeFreteNordesteTest {

    @RegisterExtension
    static StubRunnerExtension frete = new StubRunnerExtension()
            .downloadStub("dev.trilhajava", "m10-testes-aulas")
            .repoRoot("stubs://" + Path.of("target/stubs").toAbsolutePath().toUri())
            .stubsMode(StubRunnerProperties.StubsMode.REMOTE);

    @Test
    void cotaOFreteParaORecife() {
        var cliente = new ClienteDeFrete(RestClient.builder(), frete.findStubUrl("m10-testes-aulas").toString());

        CotacaoDeFrete cotacao = cliente.cotar("50030-230", 1000);

        assertThat(cotacao.valor()).isEqualByComparingTo("27.90");
        assertThat(cotacao.prazoEmDiasUteis()).isEqualTo(7);
    }
}
