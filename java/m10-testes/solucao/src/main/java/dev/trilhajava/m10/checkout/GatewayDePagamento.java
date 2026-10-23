package dev.trilhajava.m10.checkout;

import java.math.BigDecimal;

public interface GatewayDePagamento {

    /** @throws GatewayIndisponivelException se o gateway não responder */
    ResultadoPagamento cobrar(String clienteId, BigDecimal valor);
}
