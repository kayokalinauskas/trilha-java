package dev.trilhajava.m10.aulas.reembolso;

/** Controle de estoque dos produtos. */
public interface Estoque {

    /** Põe de volta no estoque as unidades devolvidas pelo cliente. */
    void devolver(String sku, int quantidade);
}
