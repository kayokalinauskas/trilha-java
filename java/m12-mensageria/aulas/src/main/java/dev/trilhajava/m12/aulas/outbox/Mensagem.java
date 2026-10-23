package dev.trilhajava.m12.aulas.outbox;

import java.util.UUID;

/** O que vai para o broker: o id do evento (header), a chave (partição), o tipo (header) e o JSON. */
public record Mensagem(UUID id, String chave, String tipo, String payload) {}
