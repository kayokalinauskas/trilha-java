package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.Transferencia;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferenciaResponse(UUID id, UUID origem, UUID destino, BigDecimal valor, Instant criadaEm) {

    static TransferenciaResponse de(Transferencia t) {
        return new TransferenciaResponse(t.id(), t.origem(), t.destino(), t.valor(), t.criadaEm());
    }
}
