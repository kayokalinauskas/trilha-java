package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Boleto;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Cartao;
import dev.trilhajava.m13.aulas.padroes.MeioDePagamento.Pix;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quanto o checkout paga ao processador por cobrança. Um switch sobre o tipo selado
 * faz o papel de Strategy (e de Visitor) sem nenhuma hierarquia extra: se alguém
 * acrescentar um meio de pagamento, este método deixa de compilar até tratá-lo.
 */
public final class Tarifas {

    private static final BigDecimal PIX = new BigDecimal("0.0099");
    private static final BigDecimal CARTAO_A_VISTA = new BigDecimal("0.0249");
    private static final BigDecimal POR_PARCELA_EXTRA = new BigDecimal("0.0150");
    private static final BigDecimal BOLETO = new BigDecimal("3.49");

    private Tarifas() {
    }

    public static BigDecimal taxa(MeioDePagamento meio, BigDecimal valor) {
        BigDecimal taxa = switch (meio) {
            case Pix _ -> valor.multiply(PIX);
            case Cartao(_, int parcelas) when parcelas == 1 -> valor.multiply(CARTAO_A_VISTA);
            case Cartao(_, int parcelas) -> valor.multiply(
                    CARTAO_A_VISTA.add(POR_PARCELA_EXTRA.multiply(BigDecimal.valueOf(parcelas - 1))));
            case Boleto _ -> BOLETO;
        };
        return taxa.setScale(2, RoundingMode.HALF_EVEN);
    }
}
