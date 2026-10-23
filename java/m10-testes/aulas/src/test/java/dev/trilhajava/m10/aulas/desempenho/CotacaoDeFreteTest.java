package dev.trilhajava.m10.aulas.desempenho;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Teste funcional: as duas versões devolvem a MESMA cotação. Desempenho se compara entre
 * versões que fazem a mesma coisa; uma otimização que muda o resultado é um bug.
 */
@SpringBootTest(properties = "frete.transportadora.latencia=0ms")
@AutoConfigureMockMvc
class CotacaoDeFreteTest {

    @Autowired
    MockMvcTester mvc;

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    void cotaUmCepDaCapital(String versao) {
        assertThat(mvc.get().uri("/{versao}/fretes/cotacao?cep=01310100", versao))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"cep": "01310100", "regiao": "Capital SP", "valor": 12.90, "prazoDias": 1}""");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    void cepForaDasFaixasDa404(String versao) {
        assertThat(mvc.get().uri("/{versao}/fretes/cotacao?cep=00000001", versao))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
