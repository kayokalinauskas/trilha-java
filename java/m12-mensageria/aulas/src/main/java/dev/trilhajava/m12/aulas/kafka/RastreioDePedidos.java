package dev.trilhajava.m12.aulas.kafka;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * A projeção que o site consulta para mostrar "seu pedido saiu para entrega". Guarda o histórico
 * de cada pedido em memória (numa aplicação real, uma tabela).
 *
 * Regras:
 * 1. Um evento só é aplicado se for a etapa seguinte à atual (CRIADO, depois PAGO...).
 * 2. Um evento de uma etapa que o pedido já passou é uma REENTREGA (o Kafka entrega pelo menos
 *    uma vez): é ignorado, sem erro.
 * 3. Um evento que pula etapas chegou FORA DE ORDEM: lança TransicaoInvalidaException.
 */
@Component
public class RastreioDePedidos {

    public enum Resultado { APLICADO, REPETIDO }

    private final Map<String, List<StatusDoPedido>> historicos = new ConcurrentHashMap<>();

    public Resultado aplicar(EventoDePedido evento) {
        var resultado = new Resultado[1];
        // compute() é atômico por chave: duas threads nunca mexem no mesmo pedido ao mesmo tempo.
        historicos.compute(evento.numeroPedido(), (numero, historico) -> {
            var etapas = historico == null ? new CopyOnWriteArrayList<StatusDoPedido>() : historico;
            StatusDoPedido atual = etapas.isEmpty() ? null : etapas.getLast();
            if (atual != null && evento.status().compareTo(atual) <= 0) {
                resultado[0] = Resultado.REPETIDO;
            } else if (evento.status().ehSeguinteDe(atual)) {
                etapas.add(evento.status());
                resultado[0] = Resultado.APLICADO;
            } else {
                throw new TransicaoInvalidaException(numero, atual, evento.status());
            }
            return etapas;
        });
        return resultado[0];
    }

    public Optional<StatusDoPedido> status(String numeroPedido) {
        return Optional.ofNullable(historicos.get(numeroPedido))
                .filter(etapas -> !etapas.isEmpty())
                .map(List::getLast);
    }

    public List<StatusDoPedido> historico(String numeroPedido) {
        return List.copyOf(historicos.getOrDefault(numeroPedido, List.of()));
    }
}
