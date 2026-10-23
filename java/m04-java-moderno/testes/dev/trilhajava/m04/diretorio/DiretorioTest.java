package dev.trilhajava.m04.diretorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 04.02 — Diretório de funcionários com Optional")
class DiretorioTest {

    private final Diretorio diretorio = new Diretorio(List.of(
            new Funcionario("001", "Helena (CEO)", "helena@empresa.com", null, true),
            new Funcionario("010", "Igor", "igor@empresa.com", "001", true),
            new Funcionario("020", "Júlia", "  ", "010", true),
            new Funcionario("030", "Kauê", null, "010", false),
            new Funcionario("040", "Lia", "lia@empresa.com", "999", true)));

    @Test
    @DisplayName("buscar devolve Optional vazio para matrícula desconhecida")
    void buscar() {
        assertThat(diretorio.buscar("010")).map(Funcionario::nome).contains("Igor");
        assertThat(diretorio.buscar("777")).isEmpty();
        assertThat(diretorio.buscar(null)).isEmpty();
    }

    @Test
    @DisplayName("buscarObrigatorio lança exceção de negócio")
    void obrigatorio() {
        assertThat(diretorio.buscarObrigatorio("001").nome()).isEqualTo("Helena (CEO)");
        assertThatThrownBy(() -> diretorio.buscarObrigatorio("777"))
                .isInstanceOf(FuncionarioNaoEncontradoException.class).hasMessageContaining("777");
    }

    @Test
    @DisplayName("e-mail do gestor encadeia buscas sem nenhum if de null")
    void emailDoGestor() {
        assertThat(diretorio.emailDoGestor("020")).contains("igor@empresa.com");
        assertThat(diretorio.emailDoGestor("001")).as("CEO não tem gestor").isEmpty();
        assertThat(diretorio.emailDoGestor("040")).as("gestor inexistente").isEmpty();
        assertThat(diretorio.emailDoGestor("777")).isEmpty();
    }

    @Test
    @DisplayName("nome para exibição tem um padrão quando não encontra")
    void nomeParaExibicao() {
        assertThat(diretorio.nomeParaExibicao("030")).isEqualTo("Kauê");
        assertThat(diretorio.nomeParaExibicao("777")).isEqualTo("Funcionário não encontrado");
    }

    @Test
    @DisplayName("contato: e-mail próprio, senão o do gestor, senão o do RH")
    void contato() {
        assertThat(diretorio.contatoPara("010")).isEqualTo("igor@empresa.com");
        assertThat(diretorio.contatoPara("020")).as("e-mail em branco conta como ausente").isEqualTo("igor@empresa.com");
        assertThat(diretorio.contatoPara("040")).isEqualTo("lia@empresa.com");
        assertThat(diretorio.contatoPara("777")).isEqualTo(Diretorio.EMAIL_RH);
    }

    @Test
    @DisplayName("sobe N níveis na hierarquia")
    void superior() {
        assertThat(diretorio.superior("020", 1)).map(Funcionario::matricula).contains("010");
        assertThat(diretorio.superior("020", 2)).map(Funcionario::matricula).contains("001");
        assertThat(diretorio.superior("020", 3)).isEmpty();
        assertThat(diretorio.superior("020", 0)).map(Funcionario::matricula).contains("020");
    }

    @Test
    @DisplayName("e-mails de uma lista de matrículas: só os existentes, ativos e com e-mail")
    void emailsAtivos() {
        assertThat(diretorio.emailsAtivos(List.of("001", "020", "030", "777", "040")))
                .containsExactly("helena@empresa.com", "lia@empresa.com");
    }
}
