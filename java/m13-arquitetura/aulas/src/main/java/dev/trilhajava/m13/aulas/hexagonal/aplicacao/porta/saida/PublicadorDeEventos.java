package dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida;

import dev.trilhajava.m13.aulas.hexagonal.dominio.RecompensaResgatada;

/** Porta de saída: avisar o resto do mundo de que um resgate aconteceu. */
public interface PublicadorDeEventos {

    void publicar(RecompensaResgatada evento);
}
