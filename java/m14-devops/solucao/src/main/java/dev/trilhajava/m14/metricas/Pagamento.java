package dev.trilhajava.m14.metricas;

import java.math.BigDecimal;
import java.util.UUID;

public record Pagamento(UUID id, String cliente, MetodoDePagamento metodo, BigDecimal valor) {

    public enum MetodoDePagamento { PIX, CARTAO, BOLETO }

    public static Pagamento de(String cliente, MetodoDePagamento metodo, String valor) {
        return new Pagamento(UUID.randomUUID(), cliente, metodo, new BigDecimal(valor));
    }
}
