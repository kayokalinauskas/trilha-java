package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Aplica todas as regras de desconto e o teto de 12% do subtotal.
 * Não conhece nenhuma regra concreta: uma regra nova é um bean novo, sem editar esta classe.
 */
@Service
public class CalculadoraDeDescontos {

    static final BigDecimal TETO = new BigDecimal("0.12");

    private final List<RegraDeDesconto> regras;

    public CalculadoraDeDescontos(List<RegraDeDesconto> regras) {
        this.regras = List.copyOf(regras);
    }

    public Orcamento orcar(Carrinho carrinho) {
        BigDecimal subtotal = carrinho.subtotal();
        List<Desconto> descontos = regras.stream()
                .flatMap(regra -> regra.avaliar(carrinho).stream())
                .toList();
        BigDecimal soma = descontos.stream().map(Desconto::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal teto = Dinheiro.arredondar(subtotal.multiply(TETO));
        BigDecimal aplicado = soma.min(teto);
        return new Orcamento(subtotal, descontos, aplicado, Dinheiro.arredondar(subtotal.subtract(aplicado)));
    }
}
