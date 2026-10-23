package dev.trilhajava.m11.aulas.owasp.erros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.function.LongFunction;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(OutputCaptureExtension.class)
class VazamentoDeErroTest {

    // O banco caiu, e a mensagem do driver traz host, porta e usuário.
    static final LongFunction<BigDecimal> BANCO_FORA = id -> {
        throw new IllegalStateException("Connection to db-pedidos-prod.interno:5432 refused (user=app_pedidos)");
    };

    static MockMvc api(Object tratador) {
        return MockMvcBuilders.standaloneSetup(new PedidoController(BANCO_FORA))
                .setControllerAdvice(tratador)
                .build();
    }

    @Test
    void tratadorQueVazaEntregaDetalhesDaInfraestrutura() throws Exception {
        String corpo = api(new TratadorQueVaza()).perform(get("/pedidos/42"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo)
                .contains("db-pedidos-prod.interno:5432")
                .contains("app_pedidos")
                .contains("java.lang.IllegalStateException")
                .contains("dev.trilhajava.m11.aulas.owasp.erros.");
    }

    @Test
    void tratadorSeguroDevolveProblemDetailsGenericoComCodigo(CapturedOutput saida) throws Exception {
        String corpo = api(new TratadorSeguro()).perform(get("/pedidos/42"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("Não foi possível concluir a operação. Informe o código ao suporte."))
                .andExpect(jsonPath("$.codigoDeRastreio").isString())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).doesNotContain("db-pedidos-prod", "app_pedidos", "Exception", "dev.trilhajava");

        // O detalhe não sumiu: está no log, com o mesmo código que o cliente recebeu.
        String codigo = JsonPath.read(corpo, "$.codigoDeRastreio");
        assertThat(saida.getOut()).contains("erro-inesperado codigo=" + codigo)
                .contains("db-pedidos-prod.interno:5432");
    }

    @Test
    void erroDoClienteRecebeExplicacaoUtilSemDetalheInterno() throws Exception {
        api(new TratadorSeguro()).perform(get("/pedidos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O parâmetro 'id' está em formato inválido."));
    }
}
