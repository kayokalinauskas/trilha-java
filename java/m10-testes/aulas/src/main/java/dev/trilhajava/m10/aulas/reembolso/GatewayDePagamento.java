package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;

/** A empresa que processa os cartões. Cada chamada é uma operação financeira de verdade. */
public interface GatewayDePagamento {

    /**
     * Devolve ao cliente parte ou todo o valor de uma cobrança.
     *
     * @return o código do estorno
     * @throws GatewayIndisponivelException se o gateway não responder
     */
    String estornar(String autorizacao, BigDecimal valor);
}
