package dev.trilhajava.m10.aulas.contrato.frete;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Classe base dos testes que o Spring Cloud Contract GERA a partir de src/test/resources/contracts.
 * Ela prepara o provedor: só a camada web e a regra de frete real, sem servidor nem rede.
 * É aqui que entraria o "estado do provedor" (dados de teste, dublês das dependências dele).
 */
@WebMvcTest(CotacaoController.class)
@Import(CalculadoraDeFrete.class)
public abstract class ContratoDeFreteBase {

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void prepararProvedor() {
        RestAssuredMockMvc.mockMvc(mvc);
    }
}
