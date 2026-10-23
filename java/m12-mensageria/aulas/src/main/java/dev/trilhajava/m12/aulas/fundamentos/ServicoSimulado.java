package dev.trilhajava.m12.aulas.fundamentos;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Um serviço vizinho de mentira: tem um tempo de resposta declarado, pode ser
 * desligado e registra os pedidos que atendeu (para vermos duplicatas).
 */
public final class ServicoSimulado implements ServicoDestino {

    private final String nome;
    private final Duration latencia;
    private final List<String> atendidos = new ArrayList<>();
    private boolean noAr = true;

    public ServicoSimulado(String nome, Duration latencia) {
        this.nome = nome;
        this.latencia = latencia;
    }

    @Override
    public String nome() {
        return nome;
    }

    public Duration latencia() {
        return latencia;
    }

    public void derrubar() {
        noAr = false;
    }

    public void religar() {
        noAr = true;
    }

    @Override
    public void processar(PedidoPago pago) {
        if (!noAr) {
            throw new ServicoIndisponivelException(nome + " fora do ar ao processar " + pago.pedidoId());
        }
        atendidos.add(pago.pedidoId());
    }

    public List<String> atendidos() {
        return List.copyOf(atendidos);
    }
}
