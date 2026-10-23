package dev.trilhajava.m12.aulas.rabbitmq;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento: o laboratório terminou um exame. O id acompanha a mensagem de ponta a ponta
 * (vai no header message_id) e permite deduplicar no consumidor.
 */
public record ExameConcluido(UUID id, String pacienteId, TipoDeExame tipo, String unidade, Instant concluidoEm) {

    public ExameConcluido {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(unidade, "unidade");
        Objects.requireNonNull(concluidoEm, "concluidoEm");
    }

    public static ExameConcluido novo(String pacienteId, TipoDeExame tipo, String unidade, Instant concluidoEm) {
        return new ExameConcluido(UUID.randomUUID(), pacienteId, tipo, unidade, concluidoEm);
    }

    /** Routing key no formato "tipo.unidade", em minúsculas: "imagem.sp", "sangue.rj". */
    public String chaveDeRoteamento() {
        return (tipo.name() + "." + unidade).toLowerCase(Locale.ROOT);
    }
}
