package dev.trilhajava.fintech.dominio;

import java.math.BigDecimal;
import java.time.Instant;

/** Um lançamento no extrato. Imutável: extrato não se edita, se estorna. */
public record Movimento(TipoMovimento tipo, BigDecimal valor, BigDecimal saldoApos, Instant dataHora) {
}
