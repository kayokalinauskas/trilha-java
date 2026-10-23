package dev.trilhajava.m13.reservas.aplicacao.porta.saida;

import dev.trilhajava.m13.reservas.dominio.Reserva;

public interface Notificador {

    void reservaConfirmada(Reserva reserva);

    void reservaCancelada(Reserva reserva);
}
