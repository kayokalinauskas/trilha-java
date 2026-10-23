package dev.trilhajava.m13.frete.legado;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.Pedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * O código que está em produção há 4 anos. Funciona — e ninguém quer mexer nele.
 * Cada transportadora nova é mais um if; cada promoção, mais uma exceção no meio.
 *
 * NÃO altere esta classe: ela é a referência de comportamento (golden master) da refatoração.
 */
public class CalculadoraDeFreteLegada {

    public List<Cotacao> cotar(Pedido p) {
        List<Cotacao> r = new ArrayList<>();
        String reg;
        char c = p.cep().charAt(0);
        if (c == '0' || c == '1' || c == '2' || c == '3') {
            reg = "SE";
        } else if (c == '4' || c == '5' || c == '6') {
            reg = "NNE";
        } else if (c == '7') {
            reg = "CO";
        } else {
            reg = "S";
        }
        int kgExcedentes = p.pesoEmGramas() <= 1000 ? 0 : (p.pesoEmGramas() - 1000 + 999) / 1000;

        // PAC e SEDEX: até 30 kg
        if (p.pesoEmGramas() <= 30000) {
            BigDecimal base = reg.equals("SE") ? new BigDecimal("18.90")
                    : reg.equals("S") ? new BigDecimal("22.90")
                    : reg.equals("CO") ? new BigDecimal("26.90")
                    : new BigDecimal("32.90");
            int prazo = reg.equals("SE") ? 5 : reg.equals("S") ? 6 : reg.equals("CO") ? 8 : 10;
            BigDecimal pac = base.add(new BigDecimal("2.50").multiply(BigDecimal.valueOf(kgExcedentes)));
            BigDecimal sedex = pac.multiply(new BigDecimal("1.8")).setScale(2, RoundingMode.HALF_EVEN);
            int prazoSedex = (prazo + 1) / 2;
            if (p.temItemFragil()) {
                // seguro de item frágil (pedido do jurídico, 2023)
                pac = pac.add(new BigDecimal("8.00"));
                sedex = sedex.add(new BigDecimal("8.00"));
            }
            if (reg.equals("SE") && p.valorDosItens().compareTo(new BigDecimal("299.00")) >= 0) {
                pac = BigDecimal.ZERO; // campanha "frete grátis Sudeste" (marketing, 2024)
            }
            r.add(new Cotacao("PAC", pac, prazo));
            r.add(new Cotacao("SEDEX", sedex, prazoSedex));
        }

        // TRANSPORTADORA: só acima de 5 kg, cobra por kg (ou fração) do peso total
        if (p.pesoEmGramas() > 5000) {
            int kg = (p.pesoEmGramas() + 999) / 1000;
            BigDecimal t = new BigDecimal("45.00").add(new BigDecimal("1.20").multiply(BigDecimal.valueOf(kg)));
            if (p.temItemFragil()) {
                t = t.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_EVEN);
            }
            r.add(new Cotacao("TRANSPORTADORA", t, 7));
        }

        // RETIRADA na loja: só na capital paulista (CEP 01000-000 a 05999-999)
        int prefixo = Integer.parseInt(p.cep().substring(0, 2));
        if (prefixo >= 1 && prefixo <= 5) {
            r.add(new Cotacao("RETIRADA", BigDecimal.ZERO, 1));
        }

        r.sort(Comparator.comparing(Cotacao::valor)
                .thenComparing(Cotacao::prazoEmDiasUteis)
                .thenComparing(Cotacao::modalidade));
        return r;
    }
}
