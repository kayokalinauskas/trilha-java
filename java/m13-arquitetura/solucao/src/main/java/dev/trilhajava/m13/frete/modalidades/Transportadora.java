package dev.trilhajava.m13.frete.modalidades;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.ModalidadeDeFrete;
import dev.trilhajava.m13.frete.Pedido;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Só acima de 5 kg: R$ 45,00 + R$ 1,20 por kg (ou fração) do peso total, 7 dias. Frágil: +15%. */
@Component
public class Transportadora implements ModalidadeDeFrete {

    private static final BigDecimal TAXA_FIXA = new BigDecimal("45.00");
    private static final BigDecimal POR_KG = new BigDecimal("1.20");
    private static final BigDecimal ADICIONAL_FRAGIL = new BigDecimal("1.15");

    @Override
    public Optional<Cotacao> cotar(Pedido pedido) {
        if (pedido.pesoEmGramas() <= 5_000) {
            return Optional.empty();
        }
        int kg = (pedido.pesoEmGramas() + 999) / 1_000;
        BigDecimal valor = TAXA_FIXA.add(POR_KG.multiply(BigDecimal.valueOf(kg)));
        if (pedido.temItemFragil()) {
            valor = valor.multiply(ADICIONAL_FRAGIL);   // o seguro da transportadora é percentual
        }
        return Optional.of(new Cotacao("TRANSPORTADORA", valor, 7));
    }
}
