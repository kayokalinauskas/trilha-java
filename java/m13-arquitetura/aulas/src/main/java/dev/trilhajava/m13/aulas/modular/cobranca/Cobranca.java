package dev.trilhajava.m13.aulas.modular.cobranca;

import java.math.BigDecimal;
import java.util.UUID;

/** Uma cobrança autorizada pela operadora. Faz parte da API: outros módulos podem consultá-la. */
public record Cobranca(UUID consulta, String paciente, BigDecimal valor, String autorizacao) {
}
