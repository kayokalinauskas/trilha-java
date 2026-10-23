package dev.trilhajava.m16.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Exercício 16.04 (parte 1) — mascarar dados pessoais antes de enviar ao modelo")
class MascaradorDePiiTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            meu CPF é 123.456.789-09, pode conferir         | meu CPF é [CPF], pode conferir
            CPF 12345678909 sem pontuação                   | CPF [CPF] sem pontuação
            escreva para ana.souza+banco@email.com.br hoje  | escreva para [EMAIL] hoje
            ligue (11) 98765-4321 depois das 18h            | ligue [TELEFONE] depois das 18h
            meu whats é +55 11 98765-4321                   | meu whats é [TELEFONE]
            fixo 11 3456-7890                               | fixo [TELEFONE]
            cartão 4111 1111 1111 1111 foi clonado          | cartão [CARTAO] foi clonado
            cartão 4111-1111-1111-1111 foi clonado          | cartão [CARTAO] foi clonado
            cartão 4111111111111111 foi clonado             | cartão [CARTAO] foi clonado
            """)
    @DisplayName("troca cada dado pessoal pelo marcador do tipo")
    void mascara(String original, String esperado) {
        assertThat(MascaradorDePii.mascarar(original)).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            paguei R$ 1.234,56 no dia 12/03/2026
            o protocolo é 20260312 e a agência 0001
            fatura de março: 3 parcelas de 99,90
            """)
    @DisplayName("valores, datas e números curtos continuam intactos")
    void naoMascaraOQueNaoEhPii(String texto) {
        assertThat(MascaradorDePii.mascarar(texto)).isEqualTo(texto);
    }

    @Test
    @DisplayName("vários dados no mesmo texto")
    void variosDados() {
        String texto = "Sou a Ana, CPF 123.456.789-09, e-mail ana@x.com, cel (21) 99876-5432, cartão final 1111.";

        assertThat(MascaradorDePii.mascarar(texto))
                .isEqualTo("Sou a Ana, CPF [CPF], e-mail [EMAIL], cel [TELEFONE], cartão final 1111.");
    }

    @Test
    @DisplayName("null continua null")
    void nulo() {
        assertThat(MascaradorDePii.mascarar(null)).isNull();
    }
}
