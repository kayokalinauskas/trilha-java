package dev.trilhajava.m13.aulas.padroes;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Outro observer do mesmo evento: 1 ponto por real inteiro pago. */
@Component
public class ProgramaDeFidelidade {

    private final Map<String, Integer> pontos = new ConcurrentHashMap<>();

    @EventListener
    public void acumular(PagamentoAprovado evento) {
        int ganhos = evento.valor().setScale(0, RoundingMode.DOWN).intValueExact();
        pontos.merge(evento.clienteId(), ganhos, Integer::sum);
    }

    public int pontosDe(String clienteId) {
        return pontos.getOrDefault(clienteId, 0);
    }
}
