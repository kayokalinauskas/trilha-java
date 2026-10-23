package dev.trilhajava.m12.aulas.rabbitmq;

/** Falha permanente: repetir não adianta (exame sem imagens, tipo que não tem laudo...). */
public class LaudoInvalidoException extends RuntimeException {

    public LaudoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
