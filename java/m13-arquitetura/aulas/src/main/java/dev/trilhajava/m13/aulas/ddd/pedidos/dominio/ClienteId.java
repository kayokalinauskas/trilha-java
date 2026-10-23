package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

/** Referência por id a outro agregado (Cliente): o Pedido guarda só o id, nunca o objeto Cliente. */
public record ClienteId(String valor) {

    public ClienteId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O id do cliente é obrigatório");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
