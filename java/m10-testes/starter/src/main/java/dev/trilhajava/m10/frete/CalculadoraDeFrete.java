package dev.trilhajava.m10.frete;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Exercício 10.01 — o código a testar (pronto e correto). Os requisitos estão no enunciado, no site.
 *
 * <p>As regras de frete da transportadora:
 *
 * <ol>
 *   <li>Peso entre 0 (exclusivo) e 30 kg (inclusivo); valor declarado >= 0; UFs válidas. Senão, IllegalArgumentException.</li>
 *   <li><b>Frete grátis</b> (R$ 0,00, sem imposto): modalidade ECONOMICA, valor declarado >= R$ 500,00 e
 *       origem e destino na MESMA região.</li>
 *   <li><b>Base</b>: R$ 12,00 na mesma UF; R$ 18,00 na mesma região; R$ 25,00 entre regiões.</li>
 *   <li><b>Peso</b>: o 1º kg está incluído; cada kg adicional (fração conta como kg inteiro) custa R$ 2,50.</li>
 *   <li><b>EXPRESSA</b>: +60% sobre (base + peso).</li>
 *   <li><b>Seguro</b>: 1% do valor declarado que passar de R$ 100,00.</li>
 *   <li><b>ICMS</b> sobre tudo: 18% na mesma UF, 12% entre UFs diferentes.</li>
 *   <li>Resultado com 2 casas, arredondamento HALF_UP.</li>
 * </ol>
 *
 * Cada regra está num método privado com o nome dela: para ver a sua suíte pegar um defeito, altere um deles
 * de propósito, rode os testes e desfaça a alteração.
 */
public class CalculadoraDeFrete implements CalculoDeFrete {

    private static final BigDecimal PESO_MAXIMO = new BigDecimal("30");
    private static final BigDecimal VALOR_FRETE_GRATIS = new BigDecimal("500");
    private static final BigDecimal FRANQUIA_SEGURO = new BigDecimal("100");
    private static final BigDecimal POR_KG_ADICIONAL = new BigDecimal("2.50");
    private static final BigDecimal FATOR_EXPRESSA = new BigDecimal("1.6");

    @Override
    public BigDecimal calcular(Envio envio) {
        validar(envio);
        if (freteGratis(envio)) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal base = base(envio);
        BigDecimal subtotal = aplicarModalidade(base.add(adicionalPorPeso(envio.pesoKg())), envio);
        subtotal = subtotal.add(seguro(envio.valorDeclarado()));
        return arredondar(subtotal.multiply(BigDecimal.ONE.add(aliquota(envio))));
    }

    private void validar(Envio envio) {
        if (envio.pesoKg() == null || envio.pesoKg().signum() <= 0 || envio.pesoKg().compareTo(PESO_MAXIMO) > 0) {
            throw new IllegalArgumentException("Peso deve estar entre 0 e 30 kg");
        }
        if (envio.valorDeclarado() == null || envio.valorDeclarado().signum() < 0) {
            throw new IllegalArgumentException("Valor declarado não pode ser negativo");
        }
        if (envio.modalidade() == null) {
            throw new IllegalArgumentException("Informe a modalidade");
        }
        Regiao.de(envio.ufOrigem());
        Regiao.de(envio.ufDestino());
    }

    private boolean freteGratis(Envio envio) {
        return envio.modalidade() == Modalidade.ECONOMICA
                && envio.valorDeclarado().compareTo(VALOR_FRETE_GRATIS) >= 0
                && mesmaRegiao(envio);
    }

    private BigDecimal base(Envio envio) {
        if (envio.ufOrigem().equals(envio.ufDestino())) {
            return new BigDecimal("12.00");
        }
        return mesmaRegiao(envio) ? new BigDecimal("18.00") : new BigDecimal("25.00");
    }

    private BigDecimal adicionalPorPeso(BigDecimal pesoKg) {
        if (pesoKg.compareTo(BigDecimal.ONE) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal quilosAdicionais = pesoKg.subtract(BigDecimal.ONE).setScale(0, RoundingMode.CEILING);
        return quilosAdicionais.multiply(POR_KG_ADICIONAL);
    }

    private BigDecimal aplicarModalidade(BigDecimal subtotal, Envio envio) {
        return envio.modalidade() == Modalidade.EXPRESSA ? subtotal.multiply(FATOR_EXPRESSA) : subtotal;
    }

    private BigDecimal seguro(BigDecimal valorDeclarado) {
        if (valorDeclarado.compareTo(FRANQUIA_SEGURO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valorDeclarado.subtract(FRANQUIA_SEGURO).multiply(new BigDecimal("0.01"));
    }

    private BigDecimal aliquota(Envio envio) {
        return envio.ufOrigem().equals(envio.ufDestino()) ? new BigDecimal("0.18") : new BigDecimal("0.12");
    }

    private BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean mesmaRegiao(Envio envio) {
        return Regiao.de(envio.ufOrigem()) == Regiao.de(envio.ufDestino());
    }
}
