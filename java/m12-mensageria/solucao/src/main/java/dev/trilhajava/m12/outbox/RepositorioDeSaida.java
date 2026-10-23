package dev.trilhajava.m12.outbox;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** A tabela de outbox. Numa aplicação real, uma tabela no mesmo banco das entidades. */
public interface RepositorioDeSaida {

    /** Até {@code limite} eventos PENDENTES, do mais antigo para o mais novo (a ordem de criação). */
    List<EventoDeSaida> pendentes(int limite);

    /** Os agregados que têm pelo menos um evento MORTO. */
    Set<String> agregadosComEventoMorto();

    Optional<EventoDeSaida> buscar(UUID id);

    /** Insere ou substitui (pelo id). */
    void salvar(EventoDeSaida evento);

    List<EventoDeSaida> todos();

    void remover(UUID id);
}
