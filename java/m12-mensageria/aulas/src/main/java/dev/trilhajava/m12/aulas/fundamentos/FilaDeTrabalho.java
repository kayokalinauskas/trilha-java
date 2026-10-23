package dev.trilhajava.m12.aulas.fundamentos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Uma fila em memória, no estilo do RabbitMQ: cada mensagem vai para UM consumidor,
 * fica "sem ack" até ser confirmada e é apagada na confirmação. Se o consumidor
 * cai sem confirmar, a mensagem volta para o início da fila marcada como reentregue.
 */
public final class FilaDeTrabalho<T> {

    public record Entrega<T>(long etiqueta, T mensagem, boolean reentregue) {
    }

    private record Pendente<T>(T mensagem, boolean reentregue) {
    }

    private final Deque<Pendente<T>> prontas = new ArrayDeque<>();
    private final Map<Long, Pendente<T>> semAck = new LinkedHashMap<>();
    private long proximaEtiqueta = 1;

    public synchronized void enviar(T mensagem) {
        prontas.addLast(new Pendente<>(mensagem, false));
    }

    public synchronized Optional<Entrega<T>> receber() {
        Pendente<T> proxima = prontas.pollFirst();
        if (proxima == null) {
            return Optional.empty();
        }
        long etiqueta = proximaEtiqueta++;
        semAck.put(etiqueta, proxima);
        return Optional.of(new Entrega<>(etiqueta, proxima.mensagem(), proxima.reentregue()));
    }

    /** ack: o consumidor terminou; a mensagem deixa de existir no broker. */
    public synchronized void confirmar(long etiqueta) {
        if (semAck.remove(etiqueta) == null) {
            throw new IllegalStateException("etiqueta desconhecida ou já confirmada: " + etiqueta);
        }
    }

    /** A conexão do consumidor caiu: tudo o que estava sem ack volta para o início da fila. */
    public synchronized void consumidorCaiu() {
        List<Pendente<T>> devolvidas = new ArrayList<>(semAck.values());
        semAck.clear();
        for (int i = devolvidas.size() - 1; i >= 0; i--) {
            prontas.addFirst(new Pendente<>(devolvidas.get(i).mensagem(), true));
        }
    }

    public synchronized int prontas() {
        return prontas.size();
    }

    public synchronized int semAck() {
        return semAck.size();
    }
}
