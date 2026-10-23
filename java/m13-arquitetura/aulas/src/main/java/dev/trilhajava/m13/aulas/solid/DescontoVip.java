package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Cliente VIP ganha 5% sobre o subtotal. */
@Component
public class DescontoVip implements RegraDeDesconto {

    private static final BigDecimal PERCENTUAL = new BigDecimal("0.05");

    @Override
    public Optional<Desconto> avaliar(Carrinho carrinho) {
        if (carrinho.tipoDeCliente() != TipoDeCliente.VIP) {
            return Optional.empty();
        }
        BigDecimal valor = Dinheiro.arredondar(carrinho.subtotal().multiply(PERCENTUAL));
        return Optional.of(new Desconto("Cliente VIP (5%)", valor));
    }
}
