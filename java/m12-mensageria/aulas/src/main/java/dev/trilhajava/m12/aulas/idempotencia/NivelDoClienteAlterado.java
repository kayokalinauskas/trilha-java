package dev.trilhajava.m12.aulas.idempotencia;

/**
 * Evento de ESTADO: carrega o nível novo inteiro, e não "suba um nível".
 *
 * @param versao cresce a cada mudança do cliente; quem a define é o produtor
 */
public record NivelDoClienteAlterado(String clienteId, String nivel, long versao) {
}
