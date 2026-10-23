package dev.trilhajava.m12.aulas.fundamentos;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/** Credita 1 ponto por real pago. Um evento processado duas vezes vira pontos em dobro. */
public final class ProgramaDeFidelidade implements ServicoDestino {

    private final Map<String, Integer> saldos = new HashMap<>();

    @Override
    public String nome() {
        return "fidelidade";
    }

    @Override
    public void processar(PedidoPago pago) {
        int pontos = pago.valor().setScale(0, RoundingMode.DOWN).intValueExact();
        saldos.merge(pago.clienteId(), pontos, Integer::sum);
    }

    public int saldo(String clienteId) {
        return saldos.getOrDefault(clienteId, 0);
    }
}
