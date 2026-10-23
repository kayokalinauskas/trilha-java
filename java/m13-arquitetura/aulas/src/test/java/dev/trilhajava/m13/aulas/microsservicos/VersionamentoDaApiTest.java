package dev.trilhajava.m13.aulas.microsservicos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import dev.trilhajava.m13.aulas.microsservicos.estoque.EstoqueAplicacao;

/** Duas versões do mesmo recurso convivendo: clientes antigos continuam funcionando. */
@SpringBootTest(classes = EstoqueAplicacao.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class VersionamentoDaApiTest {

    @LocalServerPort
    int porta;

    @Test
    void v1ContinuaNoArComAvisoEV2TrazOFormatoNovo() {
        RestClient cliente = RestClient.create("http://localhost:" + porta);

        ResponseEntity<String> v1 = cliente.get().uri("/produtos/{sku}/saldo", "FILTRO-103")
                .retrieve().toEntity(String.class);                  // cliente antigo: sem cabeçalho
        String v2 = cliente.get().uri("/produtos/{sku}/saldo", "FILTRO-103")
                .header("API-Version", "2")
                .retrieve().body(String.class);

        System.out.println("v1 -> " + v1.getBody() + " " + v1.getHeaders());
        System.out.println("v2 -> " + v2);
        assertThat(v1.getBody()).contains("\"saldo\"");
        assertThat(v1.getHeaders().getFirst("Deprecation")).isNotNull();
        assertThat(v1.getHeaders().getFirst("Sunset")).isNotNull();
        assertThat(v2).contains("\"disponivel\"").contains("\"reservado\"");
    }
}
