package dev.trilhajava.m12.aulas.retry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Um emissor de notas em memória, com instabilidade programável: falhar nas próximas N chamadas
 * ou ficar fora do ar até segunda ordem. Guarda uma nota por pedido (é idempotente).
 */
@Component
public class EmissorDeNotasSimulado implements EmissorDeNotas {

    private final Map<String, String> notas = new ConcurrentHashMap<>();
    private final AtomicInteger chamadas = new AtomicInteger();
    private final AtomicInteger falhasProgramadas = new AtomicInteger();
    private volatile boolean foraDoAr;

    @Override
    public String emitir(PedidoPago pedido) {
        chamadas.incrementAndGet();
        if (foraDoAr || falhasProgramadas.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
            throw new ServicoIndisponivelException("emissor de NF-e respondeu 503 Service Unavailable");
        }
        return notas.computeIfAbsent(pedido.numeroPedido(), numero -> "NFE-" + numero);
    }

    public void falharAsProximas(int vezes) {
        falhasProgramadas.set(vezes);
    }

    public void ficarForaDoAr(boolean foraDoAr) {
        this.foraDoAr = foraDoAr;
    }

    public int chamadas() {
        return chamadas.get();
    }

    public Map<String, String> notasEmitidas() {
        return Map.copyOf(notas);
    }
}
