package dev.trilhajava.m12.aulas.esquemas;

import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

/**
 * v1 → v2 do "pedido confirmado". Na v1 o valor era um inteiro em centavos
 * ({@code "valorEmCentavos": 11230}) e o CEP se chamava {@code cepDestino}.
 * Na v2 o valor virou objeto com quantia decimal e moeda, e o CEP virou {@code cepDeEntrega}.
 */
public final class PedidoConfirmadoV1ParaV2 implements Upcaster {

    public static final String V1 = "br.com.loja.pedidos.pedido-confirmado.v1";
    public static final String V2 = "br.com.loja.pedidos.pedido-confirmado.v2";

    @Override
    public String tipoDeOrigem() {
        return V1;
    }

    @Override
    public String tipoDeDestino() {
        return V2;
    }

    @Override
    public ObjectNode converter(ObjectNode v1) {
        ObjectNode v2 = v1.deepCopy();

        long centavos = v2.remove("valorEmCentavos").longValue();
        ObjectNode valorTotal = v2.putObject("valorTotal");
        valorTotal.put("quantia", BigDecimal.valueOf(centavos, 2).toPlainString());
        valorTotal.put("moeda", "BRL");                // a v1 só existia em reais

        v2.set("cepDeEntrega", v2.remove("cepDestino"));
        // formaDePagamento não existia na v1: fica ausente e o consumidor usa DESCONHECIDA
        return v2;
    }
}
