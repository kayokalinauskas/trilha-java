package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A regra de arredondamento da loja, num lugar só: 2 casas, meio para cima. */
final class Dinheiro {

    private Dinheiro() {
    }

    static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
