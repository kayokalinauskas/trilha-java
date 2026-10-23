package dev.trilhajava.fintech.infra.eventos;

import dev.trilhajava.fintech.dominio.EventoDeDominio;
import dev.trilhajava.fintech.dominio.TransferenciaRealizada;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * O contrato público do evento — o JSON que vai para a outbox e para o Kafka. É separado do
 * record de domínio de propósito: o domínio pode mudar sem quebrar quem consome o evento.
 *
 * O valor viaja como texto ("150.00"): número JSON vira double em muitos consumidores.
 */
public record EventoJson(UUID id, String tipo, UUID transferencia, UUID origem, UUID destino, String valor,
        Instant ocorridoEm) {

    static EventoJson de(UUID id, EventoDeDominio evento) {
        return switch (evento) {
            case TransferenciaRealizada t -> new EventoJson(id, t.tipo(), t.transferencia(), t.origem(), t.destino(),
                    t.valor().setScale(2, RoundingMode.UNNECESSARY).toPlainString(), t.ocorridoEm());
        };
    }

    TransferenciaRealizada transferenciaRealizada() {
        return new TransferenciaRealizada(transferencia, origem, destino, new BigDecimal(valor), ocorridoEm);
    }
}
