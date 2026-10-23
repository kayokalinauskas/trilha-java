package dev.trilhajava.fintech.dominio;

import java.math.BigDecimal;
import java.util.UUID;

public class SaldoInsuficienteException extends RuntimeException {

    private final BigDecimal saldoDisponivel;

    public SaldoInsuficienteException(UUID contaId, BigDecimal saldoDisponivel) {
        super("Saldo insuficiente na conta " + contaId);
        this.saldoDisponivel = saldoDisponivel;
    }

    public BigDecimal saldoDisponivel() {
        return saldoDisponivel;
    }
}
