package dev.trilhajava.m12.notificacoes;

import java.util.Objects;
import java.util.UUID;

/** Uma notificação a ser entregue por um canal. O id identifica a mensagem de ponta a ponta. */
public record Notificacao(UUID id, Canal canal, Prioridade prioridade, String destinatario, String mensagem) {

    public Notificacao {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(canal, "canal");
        Objects.requireNonNull(prioridade, "prioridade");
        Objects.requireNonNull(destinatario, "destinatario");
        Objects.requireNonNull(mensagem, "mensagem");
    }

    public static Notificacao nova(Canal canal, Prioridade prioridade, String destinatario, String mensagem) {
        return new Notificacao(UUID.randomUUID(), canal, prioridade, destinatario, mensagem);
    }
}
