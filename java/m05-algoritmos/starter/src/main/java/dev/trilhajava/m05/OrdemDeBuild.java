package dev.trilhajava.m05;

import java.util.List;
import java.util.Map;

/** Exercício 05.05 (parte 2) — ordem de build de módulos com dependências. */
public final class OrdemDeBuild {

    private OrdemDeBuild() {
    }

    /**
     * Mapa módulo → módulos dos quais ele depende. Devolve uma ordem em que cada módulo aparece
     * depois de todas as suas dependências; entre os "prontos", ordem alfabética.
     * Ciclo: CicloDeDependenciaException com os módulos envolvidos.
     */
    public static List<String> ordenar(Map<String, List<String>> dependencias) {
        throw new UnsupportedOperationException("TODO: pesquise o algoritmo de Kahn");
    }
}
