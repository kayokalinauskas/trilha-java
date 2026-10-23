package dev.trilhajava.m13.aulas.padroes;

/** Aprovada ou recusada: os dois desfechos de negócio de uma cobrança. */
public sealed interface ResultadoDaCobranca {

    record Aprovada(String codigoDeAutorizacao) implements ResultadoDaCobranca {
    }

    record Recusada(String motivo) implements ResultadoDaCobranca {
    }
}
