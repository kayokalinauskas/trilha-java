package dev.trilhajava.m13.aulas.padroes;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observer: ouve o evento sem que o checkout saiba que ele existe. */
@Component
public class EmissorDeNotaFiscal {

    private final List<String> notas = new CopyOnWriteArrayList<>();

    @EventListener
    public void emitir(PagamentoAprovado evento) {
        notas.add("NF do pedido " + evento.pedidoId() + ": R$ " + evento.valor());
    }

    public List<String> notasEmitidas() {
        return List.copyOf(notas);
    }
}
