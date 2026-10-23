package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Item com 10 unidades ou mais ganha 3% sobre o subtotal daquele item (arredondado item a item). */
@Component
public class DescontoAtacado implements RegraDeDesconto {

    private static final int QUANTIDADE_MINIMA = 10;
    private static final BigDecimal PERCENTUAL = new BigDecimal("0.03");

    @Override
    public Optional<Desconto> avaliar(Carrinho carrinho) {
        BigDecimal valor = carrinho.itens().stream()
                .filter(item -> item.quantidade() >= QUANTIDADE_MINIMA)
                .map(item -> Dinheiro.arredondar(item.subtotal().multiply(PERCENTUAL)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (valor.signum() == 0) {
            return Optional.empty();
        }
        return Optional.of(new Desconto("Atacado (3% por item com 10+ unidades)", valor));
    }
}
