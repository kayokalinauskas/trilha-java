package dev.trilhajava.m13.cambio;

/** O provedor respondeu 404: erro de quem pediu. Repetir não adianta, e não é culpa do provedor. */
public class MoedaDesconhecidaException extends RuntimeException {

    public MoedaDesconhecidaException(String moeda) {
        super("moeda desconhecida: " + moeda);
    }
}
