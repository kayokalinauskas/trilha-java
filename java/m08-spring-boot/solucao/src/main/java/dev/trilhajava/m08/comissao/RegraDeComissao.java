package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;

/**
 * Uma estratégia de comissão para um canal de venda.
 *
 * Cada implementação é um bean do Spring. A calculadora recebe TODAS elas por injeção
 * (List<RegraDeComissao>) — um canal novo é só uma classe nova, sem mexer na calculadora.
 */
public interface RegraDeComissao {

    /** O canal atendido por esta regra, ex.: "LOJA". */
    String canal();

    /** Comissão "bruta" da venda. Arredondamento e teto são responsabilidade da calculadora. */
    BigDecimal calcular(Venda venda);
}
