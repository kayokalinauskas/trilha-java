package dev.trilhajava.m12.aulas.retry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** Um provedor de e-mail em memória que registra a ordem dos envios e pode falhar para um pedido específico. */
@Component
public class ProvedorDeEmailSimulado implements ProvedorDeEmail {

    private final List<String> pedidosEnviados = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> falhasPorPedido = new ConcurrentHashMap<>();

    @Override
    public void enviar(NotaEmitida nota) {
        Integer restantes = falhasPorPedido.computeIfPresent(nota.numeroPedido(), (pedido, n) -> n - 1);
        if (restantes != null && restantes >= 0) {
            throw new ServicoIndisponivelException("provedor de e-mail: timeout depois de 5 s");
        }
        pedidosEnviados.add(nota.numeroPedido());
    }

    public void falharParaOPedido(String numeroPedido, int vezes) {
        falhasPorPedido.put(numeroPedido, vezes);
    }

    public List<String> pedidosEnviados() {
        return List.copyOf(pedidosEnviados);
    }
}
