package dev.trilhajava.m13.aulas.decisoes.frete;

import java.util.Objects;

/** O que o checkout sabe de uma entrega: quem compra, para onde vai e quanto pesa. */
public record Remessa(String clienteId, String ufDestino, int pesoEmGramas) {

    public Remessa {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(ufDestino, "ufDestino");
        if (pesoEmGramas <= 0) {
            throw new IllegalArgumentException("o peso precisa ser positivo: " + pesoEmGramas);
        }
    }
}
