package dev.trilhajava.fintech.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Marco 5: uma transferência foi concluída. O agregado é a conta de origem. */
public record TransferenciaRealizada(UUID transferencia, UUID origem, UUID destino, BigDecimal valor, Instant ocorridoEm)
        implements EventoDeDominio {

    public TransferenciaRealizada {
        Objects.requireNonNull(transferencia);
        Objects.requireNonNull(origem);
        Objects.requireNonNull(destino);
        Objects.requireNonNull(valor);
        Objects.requireNonNull(ocorridoEm);
    }

    public static TransferenciaRealizada de(Transferencia transferencia) {
        return new TransferenciaRealizada(transferencia.id(), transferencia.origem(), transferencia.destino(),
                transferencia.valor(), transferencia.criadaEm());
    }

    @Override
    public UUID agregado() {
        return origem;
    }
}
