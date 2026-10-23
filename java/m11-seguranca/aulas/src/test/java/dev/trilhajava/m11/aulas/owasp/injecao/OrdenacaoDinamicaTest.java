package dev.trilhajava.m11.aulas.owasp.injecao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class OrdenacaoDinamicaTest {

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
    void versaoVulneravelExecutaQualquerExpressaoQueOClienteMandar() {
        // Uma subconsulta escrita pelo cliente roda dentro do ORDER BY: prova de que o texto virou SQL.
        var contas = vulneravel.listar("0001", "(SELECT COUNT(*) FROM conta)");
        assertThat(contas).hasSize(2);
    }

    @Test
    void versaoVulneravelVazaOSqlNaMensagemDeErro() {
        assertThatThrownBy(() -> vulneravel.listar("0001", "saldo'"))
                .isInstanceOf(BadSqlGrammarException.class)
                .hasMessageContaining("ORDER BY saldo'");
    }

    @Test
    void allowlistAceitaOsNomesPublicos() {
        assertThat(segura.listar("0001", "saldo")).extracting(Conta::titular)
                .containsExactly("Bruno Lima", "Ana Souza");
        assertThat(segura.listar("0001", "-saldo")).extracting(Conta::titular)
                .containsExactly("Ana Souza", "Bruno Lima");
    }

    @Test
    void allowlistRecusaQualquerOutraCoisaAntesDeTocarNoBanco() {
        assertThatThrownBy(() -> segura.listar("0001", "(SELECT COUNT(*) FROM conta)"))
                .isInstanceOf(OrdenacaoInvalidaException.class)
                .hasMessage("Não é possível ordenar por '(SELECT COUNT(*) FROM conta)'."
                        + " Use um destes: [abertura, saldo, titular]");
        assertThatThrownBy(() -> segura.listar("0001", "id")) // coluna real, mas não pública
                .isInstanceOf(OrdenacaoInvalidaException.class);
    }

    @Test
    void semOrdenacaoUsaOPadrao() {
        assertThat(OrdenacaoDeContas.paraSql(null)).isEqualTo("id ASC");
        assertThat(OrdenacaoDeContas.paraSql("titular")).isEqualTo("titular ASC, id ASC");
    }
}
