package dev.trilhajava.m13.aulas.solid;

import java.util.Optional;

/**
 * Uma regra comercial de desconto. Cada regra é um bean; a calculadora recebe todas.
 *
 * <p>Contrato (vale para TODA implementação):
 * <ul>
 *   <li>devolve {@code Optional.empty()} quando a regra não se aplica, nunca {@code null};</li>
 *   <li>o valor tem 2 casas decimais e fica entre zero e o subtotal do carrinho;</li>
 *   <li>não lança exceção para um carrinho válido e não altera nada fora dela.</li>
 * </ul>
 */
public interface RegraDeDesconto {

    Optional<Desconto> avaliar(Carrinho carrinho);
}
