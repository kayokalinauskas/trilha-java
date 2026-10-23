package dev.trilhajava.m08.cambio;

public class MoedaNaoSuportadaException extends RuntimeException {

    public MoedaNaoSuportadaException(String moeda) {
        super("Moeda não suportada: " + moeda);
    }
}
