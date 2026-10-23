package dev.trilhajava.m13.aulas.ddd.pedidos.aplicacao;

import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.EventoDeDominio;
import java.util.List;

/** Porta de saída para os eventos de domínio. Em produção: Spring events + outbox, ou Kafka via outbox (aula 12.04). */
public interface PublicadorDeEventos {

    void publicar(List<EventoDeDominio> eventos);
}
