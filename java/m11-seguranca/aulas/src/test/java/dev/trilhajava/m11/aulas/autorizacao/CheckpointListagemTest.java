package dev.trilhajava.m11.aulas.autorizacao;

import static dev.trilhajava.m11.aulas.autorizacao.Tokens.como;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Solução do checkpoint da aula 11.04: a matriz da LISTAGEM (quem vê quais ids em GET /prontuarios). */
@SpringBootTest(properties = Tokens.SEGREDO)
@AutoConfigureMockMvc
class CheckpointListagemTest {

    @Autowired
    MockMvcTester mvc;

    @ParameterizedTest(name = "{0} lista {1}")
    @CsvSource(delimiter = '|', textBlock = """
            pac-ana    | 1001
            pac-bruno  | 1002
            med-carla  | 1001
            med-diego  | 1002
            atd-elisa  | 1001 1002
            med-heitor | 1003
            atd-iara   | 1003
            """)
    void listagem(String usuario, String idsEsperados) {
        Integer[] ids = Arrays.stream(idsEsperados.split(" ")).map(Integer::valueOf).toArray(Integer[]::new);

        assertThat(mvc.get().uri("/prontuarios").with(como(usuario)).exchange()).hasStatusOk()
                .bodyJson().extractingPath("$[*].id").asArray().containsExactly((Object[]) ids);
    }
}
