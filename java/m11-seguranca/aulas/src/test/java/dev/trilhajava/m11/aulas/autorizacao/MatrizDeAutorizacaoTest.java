package dev.trilhajava.m11.aulas.autorizacao;

import static dev.trilhajava.m11.aulas.autorizacao.Tokens.como;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * A matriz: usuário (papel × clínica × dono ou não) × operação × prontuário → status esperado.
 * Uma linha por combinação. Uma regra nova = linhas novas; um bug = uma linha vermelha com nome legível.
 */
@SpringBootTest(properties = Tokens.SEGREDO)
@AutoConfigureMockMvc
@DirtiesContext // o PUT grava anotações
class MatrizDeAutorizacaoTest {

    @Autowired
    MockMvcTester mvc;

    @ParameterizedTest(name = "{0} {1} prontuário {2} → {3}")
    @CsvSource(delimiter = '|', textBlock = """
            # usuário  | operação | prontuário | status | o que diz a regra
            pac-ana    | LER      | 1001       | 200    | dona do prontuário
            pac-bruno  | LER      | 1001       | 404    | outro paciente: nem sabe que existe
            pac-bruno  | LER      | 9999       | 404    | inexistente: mesma resposta
            med-carla  | LER      | 1001       | 200    | médica responsável
            med-diego  | LER      | 1001       | 404    | médico da clínica, mas não responsável
            atd-elisa  | LER      | 1001       | 200    | recepção da clínica (sem anotações)
            med-heitor | LER      | 1001       | 404    | outro tenant
            atd-iara   | LER      | 1001       | 404    | recepção de outro tenant
            pac-ana    | ANOTAR   | 1001       | 403    | dona, mas paciente não anota
            pac-bruno  | ANOTAR   | 1001       | 403    | papel errado: 403 para qualquer id, não vaza nada
            med-carla  | ANOTAR   | 1001       | 200    | médica responsável
            med-diego  | ANOTAR   | 1001       | 404    | médico que não vê o prontuário
            med-diego  | ANOTAR   | 9999       | 404    | inexistente: mesma resposta
            atd-elisa  | ANOTAR   | 1001       | 403    | recepção vê, mas não anota
            med-heitor | ANOTAR   | 1001       | 404    | outro tenant
            pac-ana    | EXAMES   | 1001       | 200    | dona
            atd-elisa  | EXAMES   | 1001       | 404    | recepção não vê exames
            med-heitor | EXAMES   | 1001       | 404    | outro tenant
            """)
    void matriz(String usuario, String operacao, long prontuario, int esperado, String regra) {
        MvcTestResult resposta = switch (operacao) {
            case "LER" -> mvc.get().uri("/prontuarios/{id}", prontuario).with(como(usuario)).exchange();
            case "EXAMES" -> mvc.get().uri("/prontuarios/{id}/exames", prontuario).with(como(usuario)).exchange();
            case "ANOTAR" -> mvc.put().uri("/prontuarios/{id}/anotacoes", prontuario).with(como(usuario))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"anotacoes": "Suspeita de enxaqueca; pedir ressonância."}""")
                    .exchange();
            default -> throw new IllegalArgumentException("Operação desconhecida: " + operacao);
        };

        assertThat(resposta.getResponse().getStatus()).as(regra).isEqualTo(esperado);
    }
}
