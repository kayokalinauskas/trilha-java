package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Os erros também são contrato: status, Content-Type e o corpo ProblemDetail. */
@WebMvcTest(ProdutoController.class)
class ProdutoControllerErrosTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    CatalogoDeProdutos catalogo;

    @Test
    void produtoInexistenteViraProblemDetail404() {
        given(catalogo.buscar("NAO-EXISTE")).willThrow(new ProdutoNaoEncontradoException("NAO-EXISTE"));

        MvcTestResult resposta = mvc.get().uri("/produtos/{sku}", "NAO-EXISTE").exchange();

        assertThat(resposta).hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "type": "https://loja.dev/problemas/produto-nao-encontrado",
                          "title": "Produto não encontrado",
                          "status": 404,
                          "detail": "Não existe produto com o SKU NAO-EXISTE",
                          "instance": "/produtos/NAO-EXISTE"
                        }""");
    }

    @Test
    void corpoInvalidoViraProblemDetail400ComOsCampos() {
        MvcTestResult resposta = mvc.post().uri("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku": "", "nome": "Café", "preco": -1, "categoria": "Mercearia"}""")
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "title": "Dados inválidos",
                          "status": 400,
                          "erros": {
                            "sku": "é obrigatório",
                            "preco": "deve ser maior que zero",
                            "quantidadeInicial": "é obrigatória"
                          }
                        }""");
        then(catalogo).shouldHaveNoInteractions();
    }

    @Test
    void jsonMalformadoTambemVira400() {
        MvcTestResult resposta = mvc.post().uri("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku": "CAFE-1KG", "preco": "quarenta"}""")
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").isEqualTo("Failed to read request");
        then(catalogo).shouldHaveNoInteractions();
    }
}
