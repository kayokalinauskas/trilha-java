package dev.trilhajava.m13.frete;

import java.math.BigDecimal;
import java.util.Objects;

/** O que o frete precisa saber de um pedido. O CEP tem 8 dígitos, sem hífen. */
public record Pedido(String cep, BigDecimal valorDosItens, int pesoEmGramas, boolean temItemFragil) {

    public Pedido {
        Objects.requireNonNull(cep, "cep");
        Objects.requireNonNull(valorDosItens, "valorDosItens");
        if (!cep.matches("\\d{8}")) {
            throw new IllegalArgumentException("CEP inválido: " + cep);
        }
        if (pesoEmGramas <= 0) {
            throw new IllegalArgumentException("peso precisa ser positivo");
        }
        if (valorDosItens.signum() < 0) {
            throw new IllegalArgumentException("valor não pode ser negativo");
        }
    }

    public Regiao regiao() {
        return Regiao.doCep(cep);
    }
}
