package dev.trilhajava.m13.aulas.decisoes.checkout;

import dev.trilhajava.m13.aulas.decisoes.frete.CalculadoraDeFrete;
import dev.trilhajava.m13.aulas.decisoes.frete.Remessa;
import java.math.BigDecimal;
import java.util.Objects;

/** Quem usa o frete: não sabe (nem precisa saber) qual regra está valendo. */
public final class ServicoDeCheckout {

    private final CalculadoraDeFrete frete;

    public ServicoDeCheckout(CalculadoraDeFrete frete) {
        this.frete = Objects.requireNonNull(frete, "frete");
    }

    public BigDecimal totalComFrete(BigDecimal subtotal, Remessa remessa) {
        return subtotal.add(frete.calcular(remessa));
    }
}
