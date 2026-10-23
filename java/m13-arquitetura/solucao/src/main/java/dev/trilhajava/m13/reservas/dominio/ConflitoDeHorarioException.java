package dev.trilhajava.m13.reservas.dominio;

public class ConflitoDeHorarioException extends RuntimeException {

    public ConflitoDeHorarioException(String sala) {
        super("a sala " + sala + " já está reservada nesse horário");
    }
}
