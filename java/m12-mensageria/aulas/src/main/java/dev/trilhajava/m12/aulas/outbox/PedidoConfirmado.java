package dev.trilhajava.m12.aulas.outbox;

import java.math.BigDecimal;
import java.util.UUID;

/** O evento de domínio que outros serviços (fidelidade, nota fiscal, logística) querem saber. */
public record PedidoConfirmado(UUID pedidoId, String cliente, String sku, int quantidade, BigDecimal total) {}
