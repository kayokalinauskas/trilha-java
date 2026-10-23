package dev.trilhajava.m13.reservas.aplicacao.porta.saida;

import dev.trilhajava.m13.reservas.dominio.Sala;
import java.util.Optional;

public interface CatalogoDeSalas {

    Optional<Sala> buscar(String id);
}
