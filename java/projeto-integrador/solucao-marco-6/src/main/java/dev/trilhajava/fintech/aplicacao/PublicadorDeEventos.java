package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.EventoDeDominio;

/**
 * Marco 5: a porta de saída para eventos. Precisa ser chamada DENTRO da transação do caso de uso:
 * a implementação grava na tabela de outbox, e o evento só existe se o negócio for confirmado.
 */
public interface PublicadorDeEventos {

    void publicar(EventoDeDominio evento);
}
