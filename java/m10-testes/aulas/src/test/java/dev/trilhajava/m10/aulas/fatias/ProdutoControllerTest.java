package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    CatalogoDeProdutos catalogo;

    static final ProdutoResposta CAFE =
            new ProdutoResposta("CAFE-1KG", "Café torrado 1 kg", new BigDecimal("49.90"), 12, "Mercearia");

    @Test
    void buscaUmProdutoExistente() {
        given(catalogo.buscar("CAFE-1KG")).willReturn(CAFE);

        MvcTestResult resposta = mvc.get().uri("/produtos/{sku}", "CAFE-1KG").exchange();

        assertThat(resposta).hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "sku": "CAFE-1KG",
                          "nome": "Café torrado 1 kg",
                          "preco": 49.90,
                          "quantidadeEmEstoque": 12,
                          "categoria": "Mercearia"
                        }""");
    }

    @Test
    void cadastraEDevolve201ComLocation() {
        given(catalogo.cadastrar(any())).willReturn(CAFE);

        MvcTestResult resposta = mvc.post().uri("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku": "CAFE-1KG", "nome": "Café torrado 1 kg", "preco": 49.90,
                         "quantidadeInicial": 12, "categoria": "Mercearia"}""")
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/produtos/CAFE-1KG");
        assertThat(resposta).bodyJson().extractingPath("$.sku").isEqualTo("CAFE-1KG");
        then(catalogo).should().cadastrar(
                new NovoProduto("CAFE-1KG", "Café torrado 1 kg", new BigDecimal("49.90"), 12, "Mercearia"));
    }
}
