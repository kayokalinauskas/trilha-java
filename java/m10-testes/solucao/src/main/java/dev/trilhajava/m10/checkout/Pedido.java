package dev.trilhajava.m10.checkout;

import java.util.List;

public record Pedido(String clienteId, List<Item> itens) {
}
