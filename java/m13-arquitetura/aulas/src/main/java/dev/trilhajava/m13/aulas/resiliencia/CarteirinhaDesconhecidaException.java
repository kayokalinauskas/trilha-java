package dev.trilhajava.m13.aulas.resiliencia;

/** Falha PERMANENTE (404): repetir dá o mesmo resultado, e não é culpa da operadora. */
public class CarteirinhaDesconhecidaException extends RuntimeException {

    public CarteirinhaDesconhecidaException(String carteirinha) {
        super("carteirinha desconhecida: " + carteirinha);
    }
}
