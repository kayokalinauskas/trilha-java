package dev.trilhajava.m13.reservas.aplicacao.porta.entrada;

import dev.trilhajava.m13.reservas.dominio.Reserva;
import java.time.LocalDateTime;

/** Porta de entrada (caso de uso). Quem a chama — um controller, uma fila, um teste — não sabe quem a implementa. */
public interface ReservarSala {

    record PedidoDeReserva(String sala, String responsavel, int participantes, LocalDateTime inicio, LocalDateTime fim) {
    }

    Reserva reservar(PedidoDeReserva pedido);
}
