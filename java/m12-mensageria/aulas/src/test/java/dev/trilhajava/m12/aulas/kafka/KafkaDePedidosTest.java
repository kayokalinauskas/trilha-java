package dev.trilhajava.m12.aulas.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

/** Um broker Kafka 4 (KRaft) de verdade, dentro da JVM do teste: sem Docker. */
@SpringBootTest(classes = AplicacaoDeRastreio.class)
@EmbeddedKafka(partitions = 3, topics = TopicosDePedidos.EVENTOS)
class KafkaDePedidosTest {

    @Autowired
    PublicadorDeEventos publicador;

    @Autowired
    RastreioDePedidos rastreio;

    static EventoDePedido evento(String numero, StatusDoPedido status) {
        return new EventoDePedido(numero, status, Instant.now());
    }

    @Test
    void mesmaChaveVaiSempreParaAMesmaParticao() {
        RecordMetadata criado = publicador.publicarEConfirmar(evento("PED-1001", StatusDoPedido.CRIADO));
        RecordMetadata pago = publicador.publicarEConfirmar(evento("PED-1001", StatusDoPedido.PAGO));

        assertThat(pago.partition()).isEqualTo(criado.partition());
        assertThat(pago.offset()).isGreaterThan(criado.offset());

        // A mesma conta que o produtor faz: hash murmur2 da chave, módulo o número de partições.
        byte[] chave = "PED-1001".getBytes(StandardCharsets.UTF_8);
        int esperada = Utils.toPositive(Utils.murmur2(chave)) % 3;
        assertThat(criado.partition()).isEqualTo(esperada);
    }

    @Test
    void osEventosDeCadaPedidoSaoConsumidosNaOrdem() {
        var pedidos = List.of("PED-2001", "PED-2002", "PED-2003", "PED-2004", "PED-2005", "PED-2006");
        var envios = new ArrayList<CompletableFuture<?>>();
        for (StatusDoPedido status : StatusDoPedido.values()) {
            for (String pedido : pedidos) {
                envios.add(publicador.publicar(evento(pedido, status)));   // assíncrono, intercalado
            }
        }
        CompletableFuture.allOf(envios.toArray(CompletableFuture[]::new)).join();

        aguardar(Duration.ofSeconds(15), () -> pedidos.stream()
                .allMatch(p -> rastreio.status(p).orElse(null) == StatusDoPedido.ENTREGUE));

        for (String pedido : pedidos) {
            assertThat(rastreio.historico(pedido)).containsExactly(StatusDoPedido.values());
        }
    }

    /** Consumir é assíncrono: espera a condição ficar verdadeira, até um prazo. */
    static void aguardar(Duration prazo, BooleanSupplier condicao) {
        long limite = System.nanoTime() + prazo.toNanos();
        while (!condicao.getAsBoolean()) {
            if (System.nanoTime() > limite) {
                throw new AssertionError("a condição não ficou verdadeira em " + prazo);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("espera interrompida", e);
            }
        }
    }
}
