package dev.trilhajava.m13.reservas.aplicacao.porta.entrada;

import java.util.UUID;

public interface CancelarReserva {

    void cancelar(UUID reserva, String solicitante);
}
