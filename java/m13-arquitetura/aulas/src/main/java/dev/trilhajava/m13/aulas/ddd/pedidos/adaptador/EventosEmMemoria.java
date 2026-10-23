package dev.trilhajava.m13.aulas.ddd.pedidos.adaptador;

import dev.trilhajava.m13.aulas.ddd.pedidos.aplicacao.PublicadorDeEventos;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.EventoDeDominio;
import java.util.ArrayList;
import java.util.List;

/** Guarda os eventos publicados numa lista, para a demonstração e os testes conferirem. */
public final class EventosEmMemoria implements PublicadorDeEventos {

    private final List<EventoDeDominio> publicados = new ArrayList<>();

    @Override
    public void publicar(List<EventoDeDominio> eventos) {
        publicados.addAll(eventos);
    }

    public List<EventoDeDominio> publicados() {
        return List.copyOf(publicados);
    }
}
