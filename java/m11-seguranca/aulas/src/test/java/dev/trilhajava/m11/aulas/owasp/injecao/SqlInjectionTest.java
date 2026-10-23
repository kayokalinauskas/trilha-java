package dev.trilhajava.m11.aulas.owasp.injecao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class SqlInjectionTest {

    // O atendente da agência 0001 só deveria ver contas da 0001.
    static final String ATAQUE = "x' OR '1'='1";

    EmbeddedDatabase banco;
    ContasVulneraveis vulneravel;
    ContasSeguras segura;

    @BeforeEach
    void subirBanco() {
        banco = BancoDeContas.novo();
        vulneravel = new ContasVulneraveis(BancoDeContas.cliente(banco));
        segura = new ContasSeguras(BancoDeContas.cliente(banco));
    }

    @AfterEach
    void derrubarBanco() {
        banco.shutdown();
    }

    @Test
    void usoNormalFuncionaNasDuasVersoes() {
        assertThat(vulneravel.buscarPorTitular("0001", "Ana Souza")).extracting(Conta::id).containsExactly(1L);
        assertThat(segura.buscarPorTitular("0001", "Ana Souza")).extracting(Conta::id).containsExactly(1L);
    }

    @Test
    void versaoVulneravelDevolveContasDeOutraAgencia() {
        var contas = vulneravel.buscarPorTitular("0001", ATAQUE);

        // WHERE agencia = '0001' AND titular = 'x' OR '1'='1'  → o OR vale para todas as linhas
        assertThat(contas).extracting(Conta::agencia).contains("0001", "0002");
        assertThat(contas).hasSize(4);
    }

    @Test
    void versaoSeguraTrataOAtaqueComoUmNomeComum() {
        assertThat(segura.buscarPorTitular("0001", ATAQUE)).isEmpty();
    }

    @Test
    void apostrofoLegitimoQuebraAVersaoVulneravelMasNaoASegura() {
        // "D'Ávila" é um sobrenome, não um ataque. A versão vulnerável nem consegue executar.
        assertThatThrownBy(() -> vulneravel.buscarPorTitular("0001", "Marcos D'Ávila"))
                .isInstanceOf(BadSqlGrammarException.class);
        assertThat(segura.buscarPorTitular("0001", "Marcos D'Ávila")).isEmpty();
    }
}
