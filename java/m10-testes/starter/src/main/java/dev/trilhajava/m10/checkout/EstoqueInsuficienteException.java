package dev.trilhajava.m10.checkout;

public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String sku) {
        super("Estoque insuficiente para " + sku);
    }
}
