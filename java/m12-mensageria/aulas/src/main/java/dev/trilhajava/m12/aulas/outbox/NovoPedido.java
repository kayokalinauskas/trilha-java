package dev.trilhajava.m12.aulas.outbox;

import java.math.BigDecimal;

/** O que chega ao caso de uso: quem compra, o quê, quanto. */
public record NovoPedido(String cliente, String sku, int quantidade, BigDecimal total) {}
