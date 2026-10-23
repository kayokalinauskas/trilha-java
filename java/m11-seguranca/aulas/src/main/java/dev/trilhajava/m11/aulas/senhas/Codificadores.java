package dev.trilhajava.m11.aulas.senhas;

import java.util.Map;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * O codificador de senhas do portal da clínica.
 *
 * <p>Hashes novos saem em Argon2id com os parâmetros mínimos da OWASP (19 MiB, 2 iterações,
 * 1 fio). Os antigos continuam conferindo pelo prefixo: {@code {bcrypt}} (custo 10, da versão
 * anterior do portal) e {@code {bcrypt-sha1}} (o SHA-1 sem salt do sistema legado, envelopado).
 */
public final class Codificadores {

    public static final String ID_ATUAL = "argon2id";

    private Codificadores() {
    }

    public static PasswordEncoder argon2idOwasp() {
        // saltLength 16 bytes, hashLength 32 bytes, parallelism 1, memory 19456 KiB, iterations 2
        return new Argon2PasswordEncoder(16, 32, 1, 19_456, 2);
    }

    public static PasswordEncoder doPortal() {
        Map<String, PasswordEncoder> porId = Map.of(
                ID_ATUAL, argon2idOwasp(),
                "bcrypt", new BCryptPasswordEncoder(10),
                "bcrypt-sha1", new EnvelopeSha1Legado());
        return new DelegatingPasswordEncoder(ID_ATUAL, porId);
    }
}
