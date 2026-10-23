package dev.trilhajava.m08.cambio;

public class CotacaoIndisponivelException extends RuntimeException {

    public CotacaoIndisponivelException(String moeda, Throwable causa) {
        super("Cotação indisponível para " + moeda, causa);
    }
}
