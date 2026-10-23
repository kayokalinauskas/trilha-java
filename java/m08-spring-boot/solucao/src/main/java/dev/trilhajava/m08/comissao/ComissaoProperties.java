package dev.trilhajava.m08.comissao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuração tipada, lida de application.properties/yml, variáveis de ambiente ou argumentos:
 *
 * <pre>
 * comissao.teto-por-venda=500.00
 * comissao.parceiro.percentual=2
 * </pre>
 *
 * Com @Validated, uma configuração inválida impede a aplicação de SUBIR (falha rápida),
 * em vez de gerar comissões erradas em produção.
 */
@Validated
@ConfigurationProperties("comissao")
public record ComissaoProperties(
        @DefaultValue("500.00") @Positive BigDecimal tetoPorVenda,
        @DefaultValue @Valid Parceiro parceiro) {

    public record Parceiro(@DefaultValue("2") @PositiveOrZero @DecimalMax("100") BigDecimal percentual) {
    }
}
