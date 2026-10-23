package dev.trilhajava.m08.reservas;

public class ReservaNaoEncontradaException extends RuntimeException {

    public ReservaNaoEncontradaException(long id) {
        super("Reserva " + id + " não existe");
    }
}
