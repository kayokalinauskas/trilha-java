package dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida;

import dev.trilhajava.m13.aulas.hexagonal.dominio.Recompensa;
import java.util.Optional;

/** Porta de saída: o catálogo de recompensas. */
public interface CatalogoDeRecompensas {

    Optional<Recompensa> porCodigo(String codigo);
}
