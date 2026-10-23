package dev.trilhajava.m13.reservas.aplicacao.porta.entrada;

import dev.trilhajava.m13.reservas.dominio.Reserva;
import java.time.LocalDate;
import java.util.List;

public interface ConsultarAgenda {

    /** As reservas ATIVAS da sala no dia, em ordem de início. */
    List<Reserva> agenda(String sala, LocalDate dia);
}
