package dev.trilhajava.m10.aulas.contrato.frete;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** O corpo de POST /fretes/cotacoes. */
public record PedidoDeCotacao(@NotBlank String cepDestino, @Positive int pesoEmGramas) {
}
