package dev.trilhajava.m02.conta;

import java.math.BigDecimal;

/** Exceção de negócio já pronta. No módulo 03 você vai projetar as suas. */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal solicitado, BigDecimal disponivel) {
        super("Saldo insuficiente: solicitado " + solicitado.toPlainString() + ", disponível " + disponivel.toPlainString());
    }
}
