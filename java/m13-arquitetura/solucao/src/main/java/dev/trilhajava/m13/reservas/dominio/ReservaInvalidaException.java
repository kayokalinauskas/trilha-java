package dev.trilhajava.m13.reservas.dominio;

/** Uma regra de negócio foi violada (horário, duração, capacidade...). */
public class ReservaInvalidaException extends RuntimeException {

    public ReservaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
