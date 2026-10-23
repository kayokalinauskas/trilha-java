package dev.trilhajava.m12.aulas.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m12.aulas.outbox.Apoio.BrokerEmMemoria;
import dev.trilhajava.m12.aulas.outbox.Apoio.Relogio;
import dev.trilhajava.m12.aulas.outbox.RelayDoOutbox.Resultado;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RelayDoOutboxTest {

    final Apoio.Banco banco = Apoio.bancoNovo();
    final BrokerEmMemoria broker = new BrokerEmMemoria();
    final Relogio relogio = new Relogio();
    final Outbox outbox = new Outbox(banco.jdbc(), relogio);
    final RelayDoOutbox relay = new RelayDoOutbox(banco.jdbc(), banco.transacao(), broker, relogio,
            100, Duration.ofSeconds(1), Duration.ofMinutes(5));

    @Test
    void falhaSeguraOsSeguintesDoMesmoAgregado() {
        gravar("pedido-A", "A1");
        gravar("pedido-A", "A2");
        gravar("pedido-B", "B1");
        broker.chavesRecusadas.add("pedido-A");

        var primeira = relay.executar();
        System.out.println("1ª rodada: " + primeira + " -> " + broker.chaves());
        assertThat(primeira).isEqualTo(new Resultado(1, 1, 1));   // B1 sai; A1 falha; A2 espera

        broker.chavesRecusadas.clear();                          // o broker voltou...
        assertThat(relay.executar()).isEqualTo(new Resultado(0, 0, 2));   // ...mas o backoff de A1 não venceu

        relogio.avancar(Duration.ofSeconds(1));
        var terceira = relay.executar();
        System.out.println("3ª rodada: " + terceira + " -> " + broker.chaves());
        assertThat(terceira).isEqualTo(new Resultado(2, 0, 0));
        assertThat(broker.recebidas).extracting(Mensagem::payload).containsExactly("B1", "A1", "A2");

        relogio.avancar(Duration.ofDays(8));
        assertThat(relay.limpar(Duration.ofDays(7))).isEqualTo(3);
    }

    @Test
    void skipLockedDeixaOutraInstanciaComOResto() throws Exception {
        gravar("pedido-A", "A1");
        gravar("pedido-B", "B1");
        gravar("pedido-C", "C1");

        var travou = new CountDownLatch(1);
        var pode = new CountDownLatch(1);
        try (var outraInstancia = Executors.newSingleThreadExecutor()) {
            // A "instância 1" trava as linhas de A e B e segura a transação aberta.
            outraInstancia.submit(() -> banco.transacao().executeWithoutResult(status -> {
                banco.jdbc().sql("""
                        SELECT id FROM evento_saida
                        WHERE agregado_id IN ('pedido-A', 'pedido-B')
                        FOR UPDATE SKIP LOCKED""").query(String.class).list();
                travou.countDown();
                aguardar(pode);
            }));
            aguardar(travou);

            var resultado = relay.executar();                  // a "instância 2" roda agora
            System.out.println("instância 2: " + resultado + " -> " + broker.chaves());
            assertThat(resultado).isEqualTo(new Resultado(1, 0, 0));
            assertThat(broker.chaves()).containsExactly("pedido-C");
            pode.countDown();
        }
    }

    private void gravar(String agregado, String payload) {
        banco.transacao().executeWithoutResult(status -> outbox.registrar("pedido", agregado, "Teste", payload));
    }

    private static void aguardar(CountDownLatch sinal) {
        try {
            if (!sinal.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("o sinal não chegou em 5 s");
            }
        } catch (InterruptedException interrompido) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrompido);
        }
    }
}
