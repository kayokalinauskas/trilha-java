package dev.trilhajava.m04.credito;

import java.util.List;

/** Resultado da avaliação: aprovada se nenhuma regra reprovou. Já pronto. */
public record Avaliacao(boolean aprovada, List<String> regrasReprovadas) {

    public Avaliacao {
        regrasReprovadas = List.copyOf(regrasReprovadas);
    }
}
