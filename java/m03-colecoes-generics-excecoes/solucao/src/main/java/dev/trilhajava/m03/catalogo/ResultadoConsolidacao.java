package dev.trilhajava.m03.catalogo;

import java.util.List;

/** Resultado da consolidação. Já pronto. */
public record ResultadoConsolidacao(List<Peca> pecas, int rejeitados) {

    public ResultadoConsolidacao {
        pecas = List.copyOf(pecas);
    }
}
