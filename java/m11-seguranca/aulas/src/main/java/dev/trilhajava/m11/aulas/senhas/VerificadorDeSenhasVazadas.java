package dev.trilhajava.m11.aulas.senhas;

/** Responde se uma senha aparece em vazamentos conhecidos. */
@FunctionalInterface
public interface VerificadorDeSenhasVazadas {

    boolean vazada(String senha);
}
