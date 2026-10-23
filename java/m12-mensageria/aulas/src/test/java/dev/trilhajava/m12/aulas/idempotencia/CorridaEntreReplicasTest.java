package dev.trilhajava.m12.aulas.idempotencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Corrida entre réplicas")
class CorridaEntreReplicasTest {

    @Test
    @DisplayName("8 réplicas recebem o mesmo evento ao mesmo tempo: um PROCESSADO e sete DUPLICADO")
    void soUmaReplicaAplica() throws Exception {
        var banco = new BancoDeTeste();
        banco.jdbc.sql("INSERT INTO conta (id, saldo) VALUES ('CT-ANA', 0)").update();
        var consumidor = new ConsumidorDeCashback(banco.jdbc, banco.transacao, (conta, valor) -> { }, BancoDeTeste.RELOGIO);
        var evento = new CashbackConcedido("EVT-3", "CT-ANA", "COMPRA-79", new BigDecimal("5.00"));

        var largada = new CountDownLatch(1);
        List<Future<Resultado>> resultados = new ArrayList<>();
        try (ExecutorService replicas = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 8; i++) {
                resultados.add(replicas.submit(() -> {
                    largada.await();
                    return consumidor.processar(evento);
                }));
            }
            largada.countDown();
        }

        List<Resultado> obtidos = new ArrayList<>();
        for (Future<Resultado> resultado : resultados) {
            obtidos.add(resultado.get());
        }
        assertThat(obtidos).containsOnlyOnce(Resultado.PROCESSADO).hasSize(8);
        assertThat(banco.jdbc.sql("SELECT saldo FROM conta WHERE id = 'CT-ANA'").query(BigDecimal.class).single())
            .isEqualByComparingTo("5.00");
    }
}
