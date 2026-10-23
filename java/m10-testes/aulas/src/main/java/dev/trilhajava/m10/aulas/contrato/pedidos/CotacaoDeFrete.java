package dev.trilhajava.m10.aulas.contrato.pedidos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * O que o serviço de PEDIDOS lê da cotação: só o valor e o prazo. É o "leitor tolerante":
 * campos que o provedor acrescentar (transportadora, modalidade...) são ignorados, e o valor,
 * que chega como texto ("23.90"), vira BigDecimal sem passar por double.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoDeFrete(BigDecimal valor, int prazoEmDiasUteis) {
}
