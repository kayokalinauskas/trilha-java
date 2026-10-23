package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;

/**
 * TODO: transforme em configuração tipada com o prefixo "comissao":
 *   comissao.teto-por-venda       (padrão 500.00, precisa ser positivo)
 *   comissao.parceiro.percentual  (padrão 2, entre 0 e 100)
 * Dica: @ConfigurationProperties, @DefaultValue e, para validar, @Validated + Bean Validation.
 */
public record ComissaoProperties(BigDecimal tetoPorVenda, Parceiro parceiro) {

    public record Parceiro(BigDecimal percentual) {
    }
}
