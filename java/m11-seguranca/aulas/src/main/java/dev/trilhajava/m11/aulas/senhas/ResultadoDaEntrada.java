package dev.trilhajava.m11.aulas.senhas;

import java.time.Instant;

/**
 * O que o login responde. Repare que não existe "e-mail não cadastrado" nem "senha errada":
 * para quem está do lado de fora, os dois são o mesmo {@link Recusada}.
 */
public sealed interface ResultadoDaEntrada {

    record Autenticada(String email) implements ResultadoDaEntrada {
    }

    record Recusada() implements ResultadoDaEntrada {
    }

    record AguardeAte(Instant instante) implements ResultadoDaEntrada {
    }

    record ExigeRedefinicao() implements ResultadoDaEntrada {
    }
}
