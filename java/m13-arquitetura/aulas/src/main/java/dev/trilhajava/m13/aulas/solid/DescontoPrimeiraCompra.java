package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Cupom PRIMEIRA10: 10% para cliente novo, limitado a R$ 50,00. */
@Component
public class DescontoPrimeiraCompra implements RegraDeDesconto {

    static final String CUPOM = "PRIMEIRA10";
    private static final BigDecimal PERCENTUAL = new BigDecimal("0.10");
    private static final BigDecimal LIMITE = new BigDecimal("50.00");

    @Override
    public Optional<Desconto> avaliar(Carrinho carrinho) {
        if (carrinho.tipoDeCliente() != TipoDeCliente.NOVO || !carrinho.temCupom(CUPOM)) {
            return Optional.empty();
        }
        BigDecimal valor = Dinheiro.arredondar(carrinho.subtotal().multiply(PERCENTUAL)).min(LIMITE);
        return Optional.of(new Desconto("Cupom " + CUPOM, valor));
    }
}
