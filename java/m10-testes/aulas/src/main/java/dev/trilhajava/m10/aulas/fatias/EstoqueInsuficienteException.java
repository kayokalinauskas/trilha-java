package dev.trilhajava.m10.aulas.fatias;

public class EstoqueInsuficienteException extends RuntimeException {

    private final int solicitado;
    private final int disponivel;

    public EstoqueInsuficienteException(String sku, int solicitado, int disponivel) {
        super("Estoque insuficiente para " + sku + ": pedido " + solicitado + ", disponível " + disponivel);
        this.solicitado = solicitado;
        this.disponivel = disponivel;
    }

    public int getSolicitado() {
        return solicitado;
    }

    public int getDisponivel() {
        return disponivel;
    }
}
