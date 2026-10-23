package dev.trilhajava.m13.aulas.hexagonal.dominio;

import java.time.Instant;

/** Evento de domínio: aconteceu um resgate. Outros sistemas (parceiro, e-mail, BI) reagem a ele. */
public record RecompensaResgatada(String resgateId, String clienteId, String recompensa,
                                  long pontos, Instant realizadoEm) {

    public static RecompensaResgatada de(Resgate resgate) {
        return new RecompensaResgatada(resgate.id(), resgate.clienteId(), resgate.recompensa(),
                resgate.debitados().quantidade(), resgate.realizadoEm());
    }
}
