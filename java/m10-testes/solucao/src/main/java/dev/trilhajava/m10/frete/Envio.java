package dev.trilhajava.m10.frete;

import java.math.BigDecimal;

public record Envio(String ufOrigem, String ufDestino, BigDecimal pesoKg, BigDecimal valorDeclarado,
        Modalidade modalidade) {
}
