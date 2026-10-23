package dev.trilhajava.m10.checkout;

public interface Estoque {

    /** true se conseguiu reservar a quantidade; false se não há estoque suficiente. */
    boolean reservar(String sku, int quantidade);

    void liberar(String sku, int quantidade);
}
