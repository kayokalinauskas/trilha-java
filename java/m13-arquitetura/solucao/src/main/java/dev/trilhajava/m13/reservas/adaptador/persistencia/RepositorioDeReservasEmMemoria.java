package dev.trilhajava.m13.reservas.adaptador.persistencia;

import dev.trilhajava.m13.reservas.aplicacao.porta.saida.RepositorioDeReservas;
import dev.trilhajava.m13.reservas.dominio.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Adaptador de saída. Trocar por JPA é escrever outra classe que implementa a mesma porta. */
@Repository
class RepositorioDeReservasEmMemoria implements RepositorioDeReservas {

    private final Map<UUID, Reserva> reservas = new ConcurrentHashMap<>();

    @Override
    public void salvar(Reserva reserva) {
        reservas.put(reserva.id(), reserva);
    }

    @Override
    public Optional<Reserva> buscar(UUID id) {
        return Optional.ofNullable(reservas.get(id));
    }

    @Override
    public List<Reserva> daSalaNoDia(String sala, LocalDate dia) {
        return reservas.values().stream()
                .filter(r -> r.sala().equals(sala) && r.periodo().inicio().toLocalDate().equals(dia))
                .toList();
    }
}
