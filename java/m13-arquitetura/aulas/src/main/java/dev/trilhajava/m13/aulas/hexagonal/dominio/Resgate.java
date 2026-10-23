package dev.trilhajava.m13.aulas.hexagonal.dominio;

import java.time.Instant;

/** O resultado de um resgate: o que saiu da carteira e quanto sobrou. */
public record Resgate(String id, String clienteId, String recompensa,
                      Pontos debitados, Pontos saldoRestante, Instant realizadoEm) {
}
