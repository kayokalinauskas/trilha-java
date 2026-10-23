package dev.trilhajava.m12.pagamentos;

import java.math.BigDecimal;

public class ValorDivergenteException extends RuntimeException {

    public ValorDivergenteException(String pedidoId, BigDecimal esperado, BigDecimal recebido) {
        super("pedido %s: esperado %s, recebido %s".formatted(pedidoId, esperado, recebido));
    }
}
