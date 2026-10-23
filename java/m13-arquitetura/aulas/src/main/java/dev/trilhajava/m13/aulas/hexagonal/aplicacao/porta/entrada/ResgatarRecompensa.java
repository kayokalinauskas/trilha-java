package dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada;

import dev.trilhajava.m13.aulas.hexagonal.dominio.Resgate;

/** Porta de entrada: o caso de uso "trocar pontos por uma recompensa". */
public interface ResgatarRecompensa {

    Resgate resgatar(ComandoDeResgate comando);
}
