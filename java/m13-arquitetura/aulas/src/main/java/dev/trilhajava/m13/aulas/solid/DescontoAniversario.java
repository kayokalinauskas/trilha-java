package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Solução do checkpoint: cupom ANIVERSARIO dá R$ 20,00 para cliente recorrente,
 * nunca mais que o subtotal. Entrou como um bean novo, sem editar a calculadora.
 */
@Component
public class DescontoAniversario implements RegraDeDesconto {

    static final String CUPOM = "ANIVERSARIO";
    private static final BigDecimal VALOR = new BigDecimal("20.00");

    @Override
    public Optional<Desconto> avaliar(Carrinho carrinho) {
        if (carrinho.tipoDeCliente() != TipoDeCliente.RECORRENTE || !carrinho.temCupom(CUPOM)) {
            return Optional.empty();
        }
        BigDecimal valor = Dinheiro.arredondar(VALOR.min(carrinho.subtotal()));
        return Optional.of(new Desconto("Cupom " + CUPOM, valor));
    }
}
