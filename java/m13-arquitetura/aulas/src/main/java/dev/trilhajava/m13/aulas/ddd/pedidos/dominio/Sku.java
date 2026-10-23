package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

/** O código do produto, como o contexto de Vendas o conhece. */
public record Sku(String codigo) {

    public Sku {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O SKU é obrigatório");
        }
    }

    @Override
    public String toString() {
        return codigo;
    }
}
