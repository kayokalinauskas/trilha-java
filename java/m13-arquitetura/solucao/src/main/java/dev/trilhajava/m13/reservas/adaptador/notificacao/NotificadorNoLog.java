package dev.trilhajava.m13.reservas.adaptador.notificacao;

import dev.trilhajava.m13.reservas.aplicacao.porta.saida.Notificador;
import dev.trilhajava.m13.reservas.dominio.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Adaptador de saída. Um adaptador de e-mail ou Slack implementaria a mesma porta. */
@Component
class NotificadorNoLog implements Notificador {

    private static final Logger log = LoggerFactory.getLogger(NotificadorNoLog.class);

    @Override
    public void reservaConfirmada(Reserva reserva) {
        log.info("reserva confirmada: {} em {} ({})", reserva.responsavel(), reserva.sala(), reserva.periodo());
    }

    @Override
    public void reservaCancelada(Reserva reserva) {
        log.info("reserva cancelada: {} em {} ({})", reserva.responsavel(), reserva.sala(), reserva.periodo());
    }
}
