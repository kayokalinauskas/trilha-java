package dev.trilhajava.m13.aulas.microsservicos.estoque;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * O "banco" do serviço de estoque: só este serviço lê e escreve nestes dados.
 * Quem está de fora (o serviço de pedidos) só enxerga a API HTTP.
 *
 * <p>As reservas são identificadas pelo id do pedido. É isso que torna a reserva
 * e a liberação idempotentes: repetir a mesma chamada não muda o resultado.
 */
@Component
public class Estoque {

    public record Reserva(String pedidoId, String sku, int quantidade) {}

    /** O pedido já foi cancelado, ou o produto não tem saldo: a reserva é recusada. */
    public static class ReservaRecusada extends RuntimeException {
        public ReservaRecusada(String mensagem) {
            super(mensagem);
        }
    }

    private final Map<String, Integer> saldos = new HashMap<>();
    private final Map<String, Reserva> reservas = new HashMap<>();
    private final Set<String> pedidosCancelados = new HashSet<>();
    private volatile Duration lentidaoSimulada = Duration.ZERO;

    public Estoque() {
        saldos.put("CAFE-500G", 10);
        saldos.put("FILTRO-103", 3);
    }

    public synchronized Reserva reservar(String pedidoId, String sku, int quantidade) {
        Reserva existente = reservas.get(pedidoId);
        if (existente != null) {
            return existente;                                   // repetição: mesma resposta
        }
        if (pedidosCancelados.contains(pedidoId)) {
            throw new ReservaRecusada("pedido " + pedidoId + " já foi cancelado");
        }
        int saldo = saldos.getOrDefault(sku, 0);
        if (saldo < quantidade) {
            throw new ReservaRecusada("saldo insuficiente de " + sku + ": " + saldo);
        }
        saldos.put(sku, saldo - quantidade);
        Reserva reserva = new Reserva(pedidoId, sku, quantidade);
        reservas.put(pedidoId, reserva);
        return reserva;
    }

    /** Compensação: devolve o saldo. Liberar algo que não existe não é erro. */
    public synchronized void liberar(String pedidoId) {
        Reserva reserva = reservas.remove(pedidoId);
        if (reserva != null) {
            saldos.merge(reserva.sku(), reserva.quantidade(), Integer::sum);
        }
        pedidosCancelados.add(pedidoId);                        // reserva atrasada será recusada
    }

    public synchronized int disponivel(String sku) {
        return saldos.getOrDefault(sku, 0);
    }

    public synchronized int reservado(String sku) {
        return reservas.values().stream()
                .filter(r -> r.sku().equals(sku))
                .mapToInt(Reserva::quantidade)
                .sum();
    }

    public synchronized Optional<Reserva> reservaDo(String pedidoId) {
        return Optional.ofNullable(reservas.get(pedidoId));
    }

    /** Só para a aula: faz o serviço demorar a responder, como num dia ruim. */
    public void simularLentidao(Duration atraso) {
        this.lentidaoSimulada = atraso;
    }

    Duration lentidaoSimulada() {
        return lentidaoSimulada;
    }
}
