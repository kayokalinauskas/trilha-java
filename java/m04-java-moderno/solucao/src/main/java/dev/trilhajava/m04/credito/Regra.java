package dev.trilhajava.m04.credito;

import java.util.function.Predicate;

/** Uma regra de negócio com nome (para explicar a reprovação) e uma condição. Já pronta. */
public record Regra(String nome, Predicate<Proposta> condicao) {

    public boolean aprova(Proposta proposta) {
        return condicao.test(proposta);
    }
}
