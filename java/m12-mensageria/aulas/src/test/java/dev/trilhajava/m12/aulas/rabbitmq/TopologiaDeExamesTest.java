package dev.trilhajava.m12.aulas.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;

/** O que a aplicação vai pedir ao broker: argumentos das filas e bindings. Sem broker. */
class TopologiaDeExamesTest {

    private final Declarables topologia = new TopologiaDeExames().topologia();

    @Test
    void filaDeLaudosEhQuorumComDlxELimiteDeReentregas() {
        Queue laudos = topologia.getDeclarablesByType(Queue.class).stream()
                .filter(fila -> fila.getName().equals(TopologiaDeExames.FILA_LAUDOS))
                .findFirst().orElseThrow();

        System.out.println(laudos.getArguments());
        assertThat(laudos.isDurable()).isTrue();
        assertThat(laudos.getArguments())
                .containsEntry("x-queue-type", "quorum")
                .containsEntry("x-delivery-limit", 5)
                .containsEntry("x-dead-letter-exchange", TopologiaDeExames.DLX);
    }

    @Test
    void bindingsDaExchangeTopic() {
        var bindings = topologia.getDeclarablesByType(Binding.class).stream()
                .filter(binding -> binding.getExchange().equals(TopologiaDeExames.EXCHANGE))
                .map(binding -> binding.getDestination() + " <- " + binding.getRoutingKey())
                .toList();

        bindings.forEach(System.out::println);
        assertThat(bindings).containsExactlyInAnyOrder(
                "exames.laudos-de-imagem <- imagem.*",
                "exames.avisos-ao-paciente <- #",
                "exames.painel-sp <- *.sp");
    }
}
