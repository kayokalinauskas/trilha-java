package dev.trilhajava.m13.reservas.aplicacao.porta.saida;

import dev.trilhajava.m13.reservas.dominio.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saída: o que o núcleo precisa de uma persistência, dito nos termos do núcleo. */
public interface RepositorioDeReservas {

    void salvar(Reserva reserva);

    Optional<Reserva> buscar(UUID id);

    /** Todas as reservas (ativas ou não) da sala que começam no dia. */
    List<Reserva> daSalaNoDia(String sala, LocalDate dia);
}
