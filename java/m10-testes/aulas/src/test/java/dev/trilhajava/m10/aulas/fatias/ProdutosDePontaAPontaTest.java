package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * A aplicação inteira: controller, advice, serviço, repositórios, transações e H2.
 * Sem @Transactional no teste: cada requisição faz commit de verdade, como em produção.
 * Por isso o teste usa um SKU só dele, em vez de contar com um banco vazio.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProdutosDePontaAPontaTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void cadastraConsultaEBaixaOEstoque() {
        assertThat(mvc.post().uri("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku": "MOEDOR-01", "nome": "Moedor manual", "preco": 159.90,
                         "quantidadeInicial": 4, "categoria": "Utilidades"}"""))
                .hasStatus(HttpStatus.CREATED);

        assertThat(mvc.post().uri("/produtos/MOEDOR-01/baixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantidade": 3}"""))
                .hasStatusOk()
                .bodyJson().extractingPath("$.quantidadeEmEstoque").isEqualTo(1);

        // A baixa anterior foi confirmada no banco: agora só resta 1.
        assertThat(mvc.post().uri("/produtos/MOEDOR-01/baixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantidade": 2}"""))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.disponivel").isEqualTo(1);

        assertThat(mvc.get().uri("/produtos/MOEDOR-01"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.categoria").isEqualTo("Utilidades");
    }
}
