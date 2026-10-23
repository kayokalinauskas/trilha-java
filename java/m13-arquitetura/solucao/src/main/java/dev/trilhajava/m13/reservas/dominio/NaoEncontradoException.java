package dev.trilhajava.m13.reservas.dominio;

/** Sala ou reserva inexistente. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
