package dev.trilhajava.m12.aulas.fundamentos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Um log de eventos em memória, no estilo de uma partição do Kafka: só acrescenta no fim,
 * nunca apaga ao consumir, e cada grupo de consumidores guarda o próprio offset
 * (a posição do PRÓXIMO registro a ler). Aqui, um grupo novo começa do início.
 */
public final class LogDeEventos<T> {

    public record Registro<T>(long offset, T valor) {
    }

    private final List<T> registros = new ArrayList<>();
    private final Map<String, Long> offsetsConfirmados = new HashMap<>();

    public synchronized long publicar(T valor) {
        registros.add(valor);
        return registros.size() - 1L;
    }

    /** Lê a partir do offset confirmado do grupo, sem mudar nada: ler não é confirmar. */
    public synchronized List<Registro<T>> ler(String grupo, int maximo) {
        int inicio = Math.toIntExact(offsetConfirmado(grupo));
        int fim = (int) Math.min(registros.size(), (long) inicio + maximo);
        List<Registro<T>> lote = new ArrayList<>();
        for (int i = inicio; i < fim; i++) {
            lote.add(new Registro<>(i, registros.get(i)));
        }
        return lote;
    }

    /** Confirma (commit) que o grupo terminou tudo ANTES de {@code proximoOffset}. */
    public synchronized void confirmar(String grupo, long proximoOffset) {
        offsetsConfirmados.put(grupo, proximoOffset);
    }

    public synchronized long offsetConfirmado(String grupo) {
        return offsetsConfirmados.getOrDefault(grupo, 0L);
    }

    /** Quantos registros o grupo ainda não confirmou (o "lag"). */
    public synchronized long atraso(String grupo) {
        return registros.size() - offsetConfirmado(grupo);
    }

    public synchronized int tamanho() {
        return registros.size();
    }
}
