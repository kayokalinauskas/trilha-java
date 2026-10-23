package dev.trilhajava.m12.aulas.esquemas;

import tools.jackson.databind.node.ObjectNode;

/**
 * Converte o {@code data} de um evento de uma versão para a seguinte (v1 → v2, v2 → v3...).
 * Cada upcaster conhece só um degrau; a cadeia aplica quantos forem necessários.
 */
public interface Upcaster {

    String tipoDeOrigem();

    String tipoDeDestino();

    ObjectNode converter(ObjectNode dadosNaVersaoDeOrigem);
}
