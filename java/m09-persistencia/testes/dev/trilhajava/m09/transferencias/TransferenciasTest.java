package dev.trilhajava.m09.transferencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

@SpringBootTest
@DisplayName("Exercício 09.03 — Transferências concorrentes: lock otimista e pessimista")
class TransferenciasTest {

    @Autowired
    ServicoDeTransferencias servico;

    @Autowired
    TransferenciasComRetentativa comRetentativa;

    @Autowired
    JdbcTemplate jdbc;

    long conta(String titular, String saldo) {
        return new SimpleJdbcInsert(jdbc).withTableName("conta").usingColumns("titular", "saldo").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("titular", titular, "saldo", new BigDecimal(saldo))).longValue();
    }

    BigDecimal saldo(long id) {
        return jdbc.queryForObject("select saldo from conta where id = ?", BigDecimal.class, id);
    }

    long versao(long id) {
        return jdbc.queryForObject("select versao from conta where id = ?", Long.class, id);
    }

    static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    /** Resultado de disparar várias tarefas ao mesmo tempo. */
    record Placar(int sucessos, int saldoInsuficiente, int conflitos) {
    }

    Placar emParalelo(int quantidade, Runnable tarefa) throws Exception {
        return emParalelo(quantidade, i -> tarefa.run());
    }

    Placar emParalelo(int quantidade, java.util.function.IntConsumer tarefa) throws Exception {
        var largada = new CountDownLatch(1);
        var sucessos = new AtomicInteger();
        var insuficiente = new AtomicInteger();
        var conflitos = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(quantidade)) {
            List<Future<?>> tarefas = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) {
                int n = i;
                tarefas.add(executor.submit(() -> {
                    largada.await();
                    try {
                        tarefa.accept(n);
                        sucessos.incrementAndGet();
                    } catch (SaldoInsuficienteException e) {
                        insuficiente.incrementAndGet();
                    } catch (OptimisticLockingFailureException e) {
                        conflitos.incrementAndGet();
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (Future<?> f : tarefas) {
                f.get(); // qualquer outra exceção falha o teste
            }
        }
        return new Placar(sucessos.get(), insuficiente.get(), conflitos.get());
    }

    @Nested
    @DisplayName("regras básicas")
    class Basicas {

        @Test
        @DisplayName("transfere e incrementa a versão (@Version) das duas contas")
        void transfere() {
            long a = conta("Ana", "100.00");
            long b = conta("Bruno", "10.00");
            servico.transferir(a, b, bd("30.00"));

            assertThat(saldo(a)).isEqualByComparingTo("70.00");
            assertThat(saldo(b)).isEqualByComparingTo("40.00");
            assertThat(versao(a)).as("versão da origem").isEqualTo(1);
            assertThat(versao(b)).as("versão do destino").isEqualTo(1);
        }

        @Test
        @DisplayName("saldo insuficiente: exceção e nada muda (nem a versão)")
        void insuficiente() {
            long a = conta("Ana", "20.00");
            long b = conta("Bruno", "0.00");
            servico.transferir(a, b, bd("5.00"));
            assertThatThrownBy(() -> servico.transferir(a, b, bd("15.01"))).isInstanceOf(SaldoInsuficienteException.class);
            assertThat(saldo(a)).isEqualByComparingTo("15.00");
            assertThat(versao(a)).isEqualTo(1);
        }

        @Test
        @DisplayName("destino inexistente: exceção e o débito da origem é desfeito (rollback)")
        void rollback() {
            long a = conta("Ana", "50.00");
            assertThatThrownBy(() -> servico.transferir(a, 999_999, bd("10"))).isInstanceOf(ContaNaoEncontradaException.class);
            assertThatThrownBy(() -> servico.transferirComLockPessimista(a, 999_999, bd("10")))
                    .isInstanceOf(ContaNaoEncontradaException.class);
            assertThat(saldo(a)).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("valor não positivo ou mesma conta → IllegalArgumentException")
        void validacoes() {
            long a = conta("Ana", "50.00");
            long b = conta("Bruno", "0.00");
            assertThatThrownBy(() -> servico.transferir(a, b, bd("0"))).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> servico.transferir(a, a, bd("1"))).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> servico.transferirComLockPessimista(a, b, bd("-1"))).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("concorrência")
    class Concorrencia {

        @Test
        @DisplayName("lock otimista sem retentativa: conflitos podem falhar, mas nenhum dinheiro some ou aparece")
        void otimistaSemRetentativa() throws Exception {
            long a = conta("Ana", "160.00");
            long b = conta("Bruno", "0.00");

            Placar placar = emParalelo(16, () -> servico.transferir(a, b, bd("10.00")));

            assertThat(saldo(a).add(saldo(b))).as("soma dos saldos").isEqualByComparingTo("160.00");
            assertThat(saldo(a)).as("saldo da origem")
                    .isEqualByComparingTo(bd("160.00").subtract(bd("10.00").multiply(BigDecimal.valueOf(placar.sucessos()))));
            assertThat(versao(a)).as("uma versão por transferência confirmada").isEqualTo(placar.sucessos());
        }

        @Test
        @DisplayName("lock otimista COM retentativa: 12 transferências de R$ 10 numa conta com R$ 100 → exatamente 10 passam")
        void otimistaComRetentativa() throws Exception {
            long a = conta("Ana", "100.00");
            long b = conta("Bruno", "0.00");

            Placar placar = emParalelo(12, () -> comRetentativa.transferir(a, b, bd("10.00")));

            assertThat(placar).isEqualTo(new Placar(10, 2, 0));
            assertThat(saldo(a)).isEqualByComparingTo("0.00");
            assertThat(saldo(b)).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("lock pessimista: 12 transferências simultâneas → exatamente 10 passam, sem nenhuma retentativa")
        void pessimista() throws Exception {
            long a = conta("Ana", "100.00");
            long b = conta("Bruno", "0.00");

            Placar placar = emParalelo(12, () -> servico.transferirComLockPessimista(a, b, bd("10.00")));

            assertThat(placar).isEqualTo(new Placar(10, 2, 0));
            assertThat(saldo(a)).isEqualByComparingTo("0.00");
            assertThat(versao(a)).isEqualTo(10);
        }

        @Test
        @DisplayName("lock pessimista em sentidos opostos (A→B e B→A ao mesmo tempo) não entra em deadlock")
        void semDeadlock() throws Exception {
            long a = conta("Ana", "100.00");
            long b = conta("Bruno", "100.00");
            long inicio = System.nanoTime();

            Placar placar = emParalelo(20, i -> {
                if (i % 2 == 0) {
                    servico.transferirComLockPessimista(a, b, bd("5.00"));
                } else {
                    servico.transferirComLockPessimista(b, a, bd("5.00"));
                }
            });

            long ms = (System.nanoTime() - inicio) / 1_000_000;
            assertThat(placar.sucessos()).isEqualTo(20);
            assertThat(saldo(a)).isEqualByComparingTo("100.00");
            assertThat(saldo(b)).isEqualByComparingTo("100.00");
            assertThat(ms).as("tempo total (um deadlock esperaria o LOCK_TIMEOUT de 10 s)").isLessThan(8_000);
        }
    }
}
