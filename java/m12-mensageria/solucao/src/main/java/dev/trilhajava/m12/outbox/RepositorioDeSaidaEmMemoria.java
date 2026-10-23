package dev.trilhajava.m12.outbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Implementação em memória, para os testes. Desempata eventos do mesmo instante pela ordem de inserção. */
public class RepositorioDeSaidaEmMemoria implements RepositorioDeSaida {

    private final Map<UUID, EventoDeSaida> eventos = new LinkedHashMap<>();
    private final Map<UUID, Long> sequencia = new LinkedHashMap<>();
    private long proximo;

    @Override
    public synchronized List<EventoDeSaida> pendentes(int limite) {
        return eventos.values().stream()
                .filter(e -> e.status() == EventoDeSaida.Status.PENDENTE)
                .sorted(Comparator.comparing(EventoDeSaida::criadoEm).thenComparing(e -> sequencia.get(e.id())))
                .limit(limite)
                .toList();
    }

    @Override
    public synchronized Set<String> agregadosComEventoMorto() {
        return eventos.values().stream()
                .filter(e -> e.status() == EventoDeSaida.Status.MORTO)
                .map(EventoDeSaida::agregado)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized Optional<EventoDeSaida> buscar(UUID id) {
        return Optional.ofNullable(eventos.get(id));
    }

    @Override
    public synchronized void salvar(EventoDeSaida evento) {
        sequencia.computeIfAbsent(evento.id(), id -> proximo++);
        eventos.put(evento.id(), evento);
    }

    @Override
    public synchronized List<EventoDeSaida> todos() {
        return new ArrayList<>(eventos.values());
    }

    @Override
    public synchronized void remover(UUID id) {
        eventos.remove(id);
        sequencia.remove(id);
    }
}
