package dev.trilhajava.m10.aulas.fatias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Solução do checkpoint: a mesma configuração do ProdutoControllerTest, portanto o mesmo contexto em cache. */
@WebMvcTest(ProdutoController.class)
class ProdutoControllerBaixaTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    CatalogoDeProdutos catalogo;

    MvcTestResult baixar(String sku, String corpo) {
        return mvc.post().uri("/produtos/{sku}/baixas", sku)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo)
                .exchange();
    }

    @Test
    void baixaDevolveOSaldo() {
        given(catalogo.baixarEstoque("CAFE-1KG", 3)).willReturn(9);

        assertThat(baixar("CAFE-1KG", """
                {"quantidade": 3}"""))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"sku": "CAFE-1KG", "quantidadeEmEstoque": 9}""");
    }

    @Test
    void estoqueInsuficienteVira422() {
        given(catalogo.baixarEstoque("CAFE-1KG", 50))
                .willThrow(new EstoqueInsuficienteException("CAFE-1KG", 50, 12));

        assertThat(baixar("CAFE-1KG", """
                {"quantidade": 50}"""))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "type": "https://loja.dev/problemas/estoque-insuficiente",
                          "status": 422,
                          "solicitado": 50,
                          "disponivel": 12
                        }""");
    }

    @Test
    void quantidadeZeroNemChegaAoServico() {
        assertThat(baixar("CAFE-1KG", """
                {"quantidade": 0}"""))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.erros.quantidade").isEqualTo("deve ser maior que zero");

        then(catalogo).should(never()).baixarEstoque(anyString(), anyInt());
    }
}
