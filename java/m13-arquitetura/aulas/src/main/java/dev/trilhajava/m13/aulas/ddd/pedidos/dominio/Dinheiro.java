package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object: um valor em reais, nunca negativo, sempre com duas casas decimais.
 * Dois Dinheiro com o mesmo valor são iguais (10.5 e 10.50 também, porque a escala é normalizada).
 */
public record Dinheiro(BigDecimal valor) {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "valor");
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("Dinheiro não pode ser negativo: " + valor);
        }
        if (valor.scale() > 2) {
            throw new IllegalArgumentException("Dinheiro tem no máximo 2 casas decimais: " + valor);
        }
        valor = valor.setScale(2);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro deCentavos(long centavos) {
        return new Dinheiro(BigDecimal.valueOf(centavos, 2));
    }

    public Dinheiro mais(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro vezes(int quantidade) {
        return new Dinheiro(valor.multiply(BigDecimal.valueOf(quantidade)));
    }

    /** Subtrai e para em zero: útil para "quanto ainda cabe" (crédito disponível, saldo). */
    public Dinheiro menosAteZero(Dinheiro outro) {
        return new Dinheiro(valor.subtract(outro.valor).max(BigDecimal.ZERO));
    }

    public boolean maiorQue(Dinheiro outro) {
        return valor.compareTo(outro.valor) > 0;
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString();
    }
}
