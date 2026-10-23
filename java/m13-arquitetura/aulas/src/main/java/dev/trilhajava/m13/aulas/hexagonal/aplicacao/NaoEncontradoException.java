package dev.trilhajava.m13.aulas.hexagonal.aplicacao;

/** A carteira ou a recompensa pedida não existe. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
