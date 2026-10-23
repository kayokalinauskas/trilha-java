package dev.trilhajava.m02.conta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Exercício 02.01 — Conta corrente com cheque especial")
class ContaCorrenteTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private ContaCorrente conta;

    @BeforeEach
    void novaConta() {
        conta = new ContaCorrente("Ana Souza", bd("500.00"));
    }

    @Nested
    @DisplayName("criação")
    class Criacao {
        @Test
        @DisplayName("começa com saldo zero e disponível igual ao limite")
        void saldoInicial() {
            assertThat(conta.getSaldo()).isEqualByComparingTo("0");
            assertThat(conta.getSaldoDisponivel()).isEqualByComparingTo("500.00");
            assertThat(conta.getTitular()).isEqualTo("Ana Souza");
        }

        @Test
        @DisplayName("titular em branco é recusado")
        void titularInvalido() {
            assertThatThrownBy(() -> new ContaCorrente("  ", BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new ContaCorrente(null, BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("limite negativo ou nulo é recusado")
        void limiteInvalido() {
            assertThatThrownBy(() -> new ContaCorrente("Ana", bd("-1"))).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new ContaCorrente("Ana", null)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("depósito e saque")
    class Movimentos {
        @Test
        @DisplayName("depósito aumenta o saldo")
        void deposito() {
            conta.depositar(bd("150.00"));
            conta.depositar(bd("50.50"));
            assertThat(conta.getSaldo()).isEqualByComparingTo("200.50");
        }

        @Test
        @DisplayName("saque pode usar o cheque especial")
        void saqueNoLimite() {
            conta.depositar(bd("100.00"));
            conta.sacar(bd("600.00"));
            assertThat(conta.getSaldo()).isEqualByComparingTo("-500.00");
            assertThat(conta.getSaldoDisponivel()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("saque além do limite é recusado e não altera nada")
        void saqueAlemDoLimite() {
            conta.depositar(bd("100.00"));
            assertThatThrownBy(() -> conta.sacar(bd("600.01")))
                    .isInstanceOf(SaldoInsuficienteException.class)
                    .hasMessageContaining("600.01");
            assertThat(conta.getSaldo()).isEqualByComparingTo("100.00");
            assertThat(conta.extrato()).hasSize(1);
        }

        @ParameterizedTest(name = "valor {0} é recusado")
        @ValueSource(strings = {"0", "-10", "0.00"})
        void valoresNaoPositivos(String valor) {
            assertThatThrownBy(() -> conta.depositar(bd(valor))).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> conta.sacar(bd(valor))).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("valor nulo é recusado")
        void nulo() {
            assertThatThrownBy(() -> conta.depositar(null)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("extrato")
    class Extrato {
        @Test
        @DisplayName("registra cada movimentação com o saldo após ela")
        void registra() {
            conta.depositar(bd("100.00"));
            conta.sacar(bd("30.00"));
            List<Movimentacao> extrato = conta.extrato();
            assertThat(extrato).hasSize(2);
            assertThat(extrato.get(0).tipo()).isEqualTo(TipoMovimentacao.CREDITO);
            assertThat(extrato.get(0).saldoApos()).isEqualByComparingTo("100.00");
            assertThat(extrato.get(1).tipo()).isEqualTo(TipoMovimentacao.DEBITO);
            assertThat(extrato.get(1).valor()).isEqualByComparingTo("30.00");
            assertThat(extrato.get(1).saldoApos()).isEqualByComparingTo("70.00");
        }

        @Test
        @DisplayName("não pode ser alterado por quem recebe a lista")
        void imutavel() {
            conta.depositar(bd("10.00"));
            List<Movimentacao> extrato = conta.extrato();
            assertThatThrownBy(extrato::clear).isInstanceOf(UnsupportedOperationException.class);
            assertThat(conta.extrato()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("transferência")
    class Transferencia {
        @Test
        @DisplayName("debita de uma conta e credita na outra")
        void transfere() {
            ContaCorrente destino = new ContaCorrente("Bruno Lima", BigDecimal.ZERO);
            conta.depositar(bd("200.00"));
            conta.transferirPara(destino, bd("80.00"));
            assertThat(conta.getSaldo()).isEqualByComparingTo("120.00");
            assertThat(destino.getSaldo()).isEqualByComparingTo("80.00");
            assertThat(conta.extrato().get(1).tipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA_ENVIADA);
            assertThat(destino.extrato().get(0).tipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA_RECEBIDA);
        }

        @Test
        @DisplayName("se falhar, nenhuma das contas muda")
        void atomica() {
            ContaCorrente destino = new ContaCorrente("Bruno Lima", BigDecimal.ZERO);
            assertThatThrownBy(() -> conta.transferirPara(destino, bd("900.00"))).isInstanceOf(SaldoInsuficienteException.class);
            assertThat(conta.getSaldo()).isEqualByComparingTo("0");
            assertThat(destino.getSaldo()).isEqualByComparingTo("0");
            assertThat(destino.extrato()).isEmpty();
        }

        @Test
        @DisplayName("não é permitido transferir para a própria conta")
        void propriaConta() {
            conta.depositar(bd("10.00"));
            assertThatThrownBy(() -> conta.transferirPara(conta, bd("1.00"))).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
