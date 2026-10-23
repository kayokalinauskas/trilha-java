package dev.trilhajava.fintech.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Testes de UNIDADE: sem Spring, sem banco, milissegundos. A base da pirâmide. */
@DisplayName("Conta (regras de domínio)")
class ContaTest {

    static final Instant AGORA = Instant.parse("2026-09-28T13:00:00Z");

    static Conta novaConta() {
        return new Conta(UUID.randomUUID(), "Ana", new Cpf("52998224725"), TipoConta.CORRENTE, AGORA);
    }

    static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    @DisplayName("nasce com saldo zero e duas casas decimais")
    void saldoInicial() {
        assertThat(novaConta().saldo()).isEqualByComparingTo("0").hasScaleOf(2);
    }

    @Test
    @DisplayName("depósito aumenta o saldo e gera o movimento com o saldo após")
    void deposito() {
        Conta conta = novaConta();
        Movimento movimento = conta.depositar(bd("150.5"), AGORA);

        assertThat(conta.saldo()).isEqualByComparingTo("150.50");
        assertThat(movimento.tipo()).isEqualTo(TipoMovimento.DEPOSITO);
        assertThat(movimento.valor()).isEqualByComparingTo("150.50");
        assertThat(movimento.saldoApos()).isEqualByComparingTo("150.50");
        assertThat(movimento.dataHora()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("saque maior que o saldo: SaldoInsuficienteException com o saldo disponível, e nada muda")
    void saqueSemSaldo() {
        Conta conta = novaConta();
        conta.depositar(bd("100.00"), AGORA);

        assertThatThrownBy(() -> conta.sacar(bd("100.01"), AGORA))
                .isInstanceOfSatisfying(SaldoInsuficienteException.class,
                        e -> assertThat(e.saldoDisponivel()).isEqualByComparingTo("100.00"));
        assertThat(conta.saldo()).isEqualByComparingTo("100.00");
    }

    @ParameterizedTest(name = "valor {0}")
    @NullSource
    @ValueSource(strings = {"0", "-1", "0.001"})
    @DisplayName("valores nulos, não positivos ou com mais de 2 casas são recusados")
    void valoresInvalidos(String valor) {
        Conta conta = novaConta();
        BigDecimal quantia = valor == null ? null : bd(valor);
        assertThatIllegalArgumentException().isThrownBy(() -> conta.depositar(quantia, AGORA));
        assertThatIllegalArgumentException().isThrownBy(() -> conta.sacar(quantia, AGORA));
    }

    @Test
    @DisplayName("transferência: a origem registra TRANSFERENCIA_ENVIADA e o destino, TRANSFERENCIA_RECEBIDA")
    void transferencia() {
        Conta origem = novaConta();
        Conta destino = novaConta();
        origem.depositar(bd("80.00"), AGORA);
        var transferencia = new Transferencia(UUID.randomUUID(), origem.id(), destino.id(), bd("30.00"), AGORA, "chave");

        Movimento enviado = origem.enviar(transferencia);
        Movimento recebido = destino.receber(transferencia);

        assertThat(origem.saldo()).isEqualByComparingTo("50.00");
        assertThat(destino.saldo()).isEqualByComparingTo("30.00");
        assertThat(enviado.tipo()).isEqualTo(TipoMovimento.TRANSFERENCIA_ENVIADA);
        assertThat(recebido.tipo()).isEqualTo(TipoMovimento.TRANSFERENCIA_RECEBIDA);
        assertThat(enviado.transferenciaId()).isEqualTo(transferencia.id());
    }
}
