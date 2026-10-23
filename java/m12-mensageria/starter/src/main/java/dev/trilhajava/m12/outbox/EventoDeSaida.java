package dev.trilhajava.m12.outbox;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Uma linha da tabela de outbox: o evento gravado na MESMA transação da mudança de negócio,
 * esperando o relay publicá-lo no broker.
 *
 * @param agregado    a entidade de origem (ex.: "conta-42"): eventos do mesmo agregado saem em ordem
 * @param proximaTentativa  antes deste instante, o relay não tenta publicar de novo
 */
public record EventoDeSaida(
        UUID id,
        String agregado,
        String tipo,
        String payload,
        Instant criadoEm,
        Status status,
        int tentativas,
        Instant proximaTentativa,
        Instant publicadoEm,
        String ultimoErro) {

    public enum Status { PENDENTE, PUBLICADO, MORTO }

    public EventoDeSaida {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(agregado, "agregado");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(criadoEm, "criadoEm");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(proximaTentativa, "proximaTentativa");
    }

    public static EventoDeSaida novo(String agregado, String tipo, String payload, Instant agora) {
        return new EventoDeSaida(UUID.randomUUID(), agregado, tipo, payload, agora,
                Status.PENDENTE, 0, agora, null, null);
    }

    public EventoDeSaida publicado(Instant agora) {
        return new EventoDeSaida(id, agregado, tipo, payload, criadoEm, Status.PUBLICADO, tentativas,
                proximaTentativa, agora, ultimoErro);
    }

    /** Uma tentativa a mais que falhou; a próxima fica para {@code proxima}. */
    public EventoDeSaida falhou(String erro, Instant proxima) {
        return new EventoDeSaida(id, agregado, tipo, payload, criadoEm, Status.PENDENTE, tentativas + 1,
                proxima, null, erro);
    }

    /** Uma tentativa a mais que falhou, e foi a última permitida. */
    public EventoDeSaida morto(String erro) {
        return new EventoDeSaida(id, agregado, tipo, payload, criadoEm, Status.MORTO, tentativas + 1,
                proximaTentativa, null, erro);
    }

    /** Volta para a fila, com o contador zerado (intervenção manual depois de corrigir a causa). */
    public EventoDeSaida reativado(Instant agora) {
        return new EventoDeSaida(id, agregado, tipo, payload, criadoEm, Status.PENDENTE, 0, agora, null, ultimoErro);
    }
}
