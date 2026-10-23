package dev.trilhajava.m11.aulas.senhas;

import static dev.trilhajava.m11.aulas.senhas.PoliticaDeSenha.Problema.CURTA;
import static dev.trilhajava.m11.aulas.senhas.PoliticaDeSenha.Problema.LONGA;
import static dev.trilhajava.m11.aulas.senhas.PoliticaDeSenha.Problema.PALAVRA_DO_CONTEXTO;
import static dev.trilhajava.m11.aulas.senhas.PoliticaDeSenha.Problema.VAZADA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaDeSenhaTest {

    private final Set<String> vazadas = Set.of("123456789012345", "iloveyou2026!!!");
    private final PoliticaDeSenha politica =
            new PoliticaDeSenha(senha -> vazadas.contains(senha), Set.of("vitalis", "clinica"));

    @Test
    @DisplayName("Frase longa sem maiúscula, número ou símbolo é aceita")
    void semComposicao() {
        assertThat(politica.avaliar("marta@email.com", "cachorro late no quintal")).isEmpty();
    }

    @Test
    @DisplayName("Menos de 15 caracteres é curta; mais de 64 é longa")
    void tamanho() {
        assertThat(politica.avaliar("marta@email.com", "Tr0ub4dor&3xyz")).containsExactly(CURTA);
        assertThat(politica.avaliar("marta@email.com", "abcdefghijklmno")).isEmpty();
        assertThat(politica.avaliar("marta@email.com", "x".repeat(65))).containsExactly(LONGA);
    }

    @Test
    @DisplayName("O tamanho conta code points: 15 emojis são 15 caracteres, não 30")
    void contaCodePoints() {
        String quinzeEmojis = "🌵".repeat(15); // cacto, fora do BMP: 2 chars cada

        assertThat(quinzeEmojis.length()).isEqualTo(30);
        assertThat(politica.avaliar("marta@email.com", quinzeEmojis)).isEmpty();
    }

    @Test
    @DisplayName("Senha vazada é recusada")
    void vazada() {
        assertThat(politica.avaliar("marta@email.com", "123456789012345")).containsExactly(VAZADA);
    }

    @Test
    @DisplayName("Nome do serviço e usuário do e-mail não podem aparecer, sem diferenciar maiúsculas")
    void palavrasDoContexto() {
        assertThat(politica.avaliar("marta@email.com", "minha senha da VITALIS")).containsExactly(PALAVRA_DO_CONTEXTO);
        assertThat(politica.avaliar("marta@email.com", "Marta adora pão de queijo")).containsExactly(PALAVRA_DO_CONTEXTO);
    }

    @Test
    @DisplayName("Vários problemas aparecem juntos, e a consulta de vazadas só roda se o resto passou")
    void variosProblemas() {
        var consultas = new int[1];
        var contando = new PoliticaDeSenha(senha -> {
            consultas[0]++;
            return false;
        }, Set.of("vitalis"));

        assertThat(contando.avaliar("marta@email.com", "marta")).containsExactly(CURTA, PALAVRA_DO_CONTEXTO);
        assertThat(consultas[0]).isZero();
    }
}
