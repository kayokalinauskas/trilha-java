package dev.trilhajava.m02.dinheiro;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exercício 02.03 — Value object Dinheiro.
 * Rode: mvn test -Dtest=DinheiroTest
 *
 * Um record já te dá equals/hashCode/toString. Seu trabalho: garantir as invariantes
 * no construtor compacto e implementar as operações sem nunca modificar a instância.
 */
public record Dinheiro(BigDecimal valor, Moeda moeda) {

    public Dinheiro {
        // TODO: valide (valor e moeda obrigatórios) e normalize o valor para 2 casas, HALF_EVEN.
        // Lembre: no construtor compacto você pode REATRIBUIR o parâmetro: valor = ...;
    }

    public static Dinheiro de(String valor, Moeda moeda) {
        throw new UnsupportedOperationException("TODO");
    }

    public static Dinheiro reais(String valor) {
        throw new UnsupportedOperationException("TODO");
    }

    public static Dinheiro zero(Moeda moeda) {
        throw new UnsupportedOperationException("TODO");
    }

    public Dinheiro somar(Dinheiro outro) {
        throw new UnsupportedOperationException("TODO");
    }

    public Dinheiro subtrair(Dinheiro outro) {
        throw new UnsupportedOperationException("TODO");
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean ehNegativo() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean ehZero() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Divide em N partes cuja soma é exatamente o valor original; centavos extras vão para as primeiras partes. */
    public List<Dinheiro> ratear(int partes) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Ex.: "R$ 1.234,56", "-US$ 5,00", "€ 0,50". */
    public String formatado() {
        throw new UnsupportedOperationException("TODO");
    }
}
