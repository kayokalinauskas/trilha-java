package dev.trilhajava.m08.reservas;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ServicoDeReservas {

    private static final int ESTADIA_MAXIMA = 30;

    private final Map<Long, Reserva> reservas = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    public synchronized Reserva reservar(NovaReserva pedido) {
        if (!pedido.checkOut().isAfter(pedido.checkIn())) {
            throw new PeriodoInvalidoException("o check-out deve ser depois do check-in");
        }
        if (ChronoUnit.DAYS.between(pedido.checkIn(), pedido.checkOut()) > ESTADIA_MAXIMA) {
            throw new PeriodoInvalidoException("a estadia máxima é de " + ESTADIA_MAXIMA + " noites");
        }
        boolean ocupado = reservas.values().stream()
                .anyMatch(r -> r.conflitaCom(pedido.quartoId(), pedido.checkIn(), pedido.checkOut()));
        if (ocupado) {
            throw new QuartoIndisponivelException(pedido.quartoId(), pedido.checkIn(), pedido.checkOut());
        }
        var reserva = new Reserva(sequencia.incrementAndGet(), pedido.quartoId(), pedido.hospede(),
                pedido.checkIn(), pedido.checkOut());
        reservas.put(reserva.id(), reserva);
        return reserva;
    }

    public Reserva buscar(long id) {
        Reserva reserva = reservas.get(id);
        if (reserva == null) {
            throw new ReservaNaoEncontradaException(id);
        }
        return reserva;
    }

    public void cancelar(long id) {
        if (reservas.remove(id) == null) {
            throw new ReservaNaoEncontradaException(id);
        }
    }
}
