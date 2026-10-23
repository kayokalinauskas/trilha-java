package dev.trilhajava.m12.aulas.rabbitmq;

import java.util.UUID;

/** O broker não confirmou a publicação: a mensagem pode não ter sido gravada. */
public class PublicacaoNaoConfirmadaException extends RuntimeException {

    public PublicacaoNaoConfirmadaException(UUID exameId, String motivo) {
        super("Exame " + exameId + " não confirmado pelo broker: " + motivo);
    }
}
