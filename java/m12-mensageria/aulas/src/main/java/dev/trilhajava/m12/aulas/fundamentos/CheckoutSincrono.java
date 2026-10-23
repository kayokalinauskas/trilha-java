package dev.trilhajava.m12.aulas.fundamentos;

import java.time.Duration;
import java.util.List;

/**
 * A primeira versão de quase todo sistema: o checkout chama cada vizinho, em sequência,
 * e só responde ao cliente quando todos terminaram.
 */
public final class CheckoutSincrono {

    private final List<ServicoSimulado> vizinhos;

    public CheckoutSincrono(List<ServicoSimulado> vizinhos) {
        this.vizinhos = List.copyOf(vizinhos);
    }

    /**
     * Devolve o tempo que o cliente esperou. Se QUALQUER vizinho estiver fora do ar,
     * a exceção sobe e o checkout inteiro falha: isso é acoplamento temporal.
     */
    public Duration pagar(PedidoPago pago) {
        Duration esperaDoCliente = Duration.ZERO;
        for (ServicoSimulado vizinho : vizinhos) {
            vizinho.processar(pago);
            esperaDoCliente = esperaDoCliente.plus(vizinho.latencia());
        }
        return esperaDoCliente;
    }

    /** Disponibilidade de uma cadeia em que TODOS precisam responder: o produto de cada uma. */
    public static double disponibilidadeDaCadeia(double... disponibilidades) {
        double total = 1.0;
        for (double d : disponibilidades) {
            total *= d;
        }
        return total;
    }
}
