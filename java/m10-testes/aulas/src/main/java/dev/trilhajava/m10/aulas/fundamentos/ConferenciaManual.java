package dev.trilhajava.m10.aulas.fundamentos;

import java.math.BigDecimal;

/**
 * O "teste" que quase todo mundo escreve antes de aprender a testar:
 * imprimir alguns resultados e conferir de olho.
 */
public class ConferenciaManual {

    public static void main(String[] args) {
        var calculadora = new CalculadoraDeDesconto();

        System.out.println("R$  50,00 avulso    -> " + calculadora.calcular(new BigDecimal("50.00"), Cliente.AVULSO));
        System.out.println("R$ 150,00 avulso    -> " + calculadora.calcular(new BigDecimal("150.00"), Cliente.AVULSO));
        System.out.println("R$ 150,00 assinante -> " + calculadora.calcular(new BigDecimal("150.00"), Cliente.ASSINANTE));
        System.out.println("R$ 400,00 assinante -> " + calculadora.calcular(new BigDecimal("400.00"), Cliente.ASSINANTE));
    }
}
