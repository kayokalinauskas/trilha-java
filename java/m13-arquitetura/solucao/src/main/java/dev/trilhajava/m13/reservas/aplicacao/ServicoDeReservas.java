package dev.trilhajava.m13.reservas.aplicacao;

import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.CancelarReserva;
import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.ConsultarAgenda;
import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.ReservarSala;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.CatalogoDeSalas;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.Notificador;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.RepositorioDeReservas;
import dev.trilhajava.m13.reservas.dominio.ConflitoDeHorarioException;
import dev.trilhajava.m13.reservas.dominio.NaoEncontradoException;
import dev.trilhajava.m13.reservas.dominio.Periodo;
import dev.trilhajava.m13.reservas.dominio.Reserva;
import dev.trilhajava.m13.reservas.dominio.Sala;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * O núcleo da aplicação: implementa as portas de entrada usando só as portas de saída.
 * Não conhece HTTP, JSON, banco, e-mail — nem o Spring: é testado com fakes, com um `new`.
 * Quem o registra como bean é a {@code MontagemDoNucleo}, fora das camadas.
 */
public class ServicoDeReservas implements ReservarSala, CancelarReserva, ConsultarAgenda {

    private final RepositorioDeReservas reservas;
    private final CatalogoDeSalas salas;
    private final Notificador notificador;
    private final Clock relogio;

    public ServicoDeReservas(RepositorioDeReservas reservas, CatalogoDeSalas salas, Notificador notificador,
            Clock relogio) {
        this.reservas = reservas;
        this.salas = salas;
        this.notificador = notificador;
        this.relogio = relogio;
    }

    /** synchronized: a verificação de conflito e a gravação não podem se intercalar (numa instância). */
    @Override
    public synchronized Reserva reservar(PedidoDeReserva pedido) {
        Sala sala = salas.buscar(pedido.sala())
                .orElseThrow(() -> new NaoEncontradoException("sala " + pedido.sala()));
        Reserva nova = Reserva.nova(sala, pedido.responsavel(), pedido.participantes(),
                new Periodo(pedido.inicio(), pedido.fim()), LocalDateTime.now(relogio));
        boolean conflito = reservas.daSalaNoDia(sala.id(), pedido.inicio().toLocalDate()).stream()
                .anyMatch(nova::conflitaCom);
        if (conflito) {
            throw new ConflitoDeHorarioException(sala.id());
        }
        reservas.salvar(nova);
        notificador.reservaConfirmada(nova);
        return nova;
    }

    @Override
    public synchronized void cancelar(UUID id, String solicitante) {
        Reserva reserva = reservas.buscar(id).orElseThrow(() -> new NaoEncontradoException("reserva " + id));
        boolean estavaAtiva = reserva.ativa();
        reserva.cancelar(solicitante, LocalDateTime.now(relogio));
        reservas.salvar(reserva);
        if (estavaAtiva) {
            notificador.reservaCancelada(reserva);
        }
    }

    @Override
    public List<Reserva> agenda(String sala, LocalDate dia) {
        salas.buscar(sala).orElseThrow(() -> new NaoEncontradoException("sala " + sala));
        return reservas.daSalaNoDia(sala, dia).stream()
                .filter(Reserva::ativa)
                .sorted(Comparator.comparing(r -> r.periodo().inicio()))
                .toList();
    }
}
