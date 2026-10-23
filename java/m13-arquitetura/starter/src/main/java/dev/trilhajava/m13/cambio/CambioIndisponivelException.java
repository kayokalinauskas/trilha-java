package dev.trilhajava.m13.cambio;

/** O provedor falhou (ou o circuito está aberto) e não há um último valor recente para devolver. */
public class CambioIndisponivelException extends RuntimeException {

    public CambioIndisponivelException(String moeda, Throwable causa) {
        super("cotação de " + moeda + " indisponível", causa);
    }
}
