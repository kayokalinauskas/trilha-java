package dev.trilhajava.m11.senhas;

import java.time.Instant;

/** O que guardamos de cada usuário. Repare: nenhuma senha — só o HASH, com o prefixo do algoritmo. */
public record Usuario(String email, String hashDaSenha, int falhasSeguidas, Instant bloqueadoAte) {

    public static Usuario novo(String email, String hashDaSenha) {
        return new Usuario(email, hashDaSenha, 0, null);
    }
}
