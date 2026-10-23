package dev.trilhajava.m11.aulas.senhas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Hash "em camadas" para o legado: {@code bcrypt(sha1(senha))}.
 *
 * <p>O sistema antigo guardava SHA-1 sem salt. Sem a senha original não dá para gerar um Argon2id,
 * mas dá para proteger o SHA-1 que já existe com um BCrypt por cima, numa migração em lote, sem
 * esperar ninguém entrar. No próximo login com sucesso, o {@code upgradeEncoding} devolve
 * {@code true} e a conta ganha um hash moderno de verdade.
 */
public class EnvelopeSha1Legado implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

    /** Usado só pela migração em lote: recebe o SHA-1 que já estava no banco. */
    public String envelopar(String sha1Legado) {
        return bcrypt.encode(sha1Legado.toLowerCase(Locale.ROOT));
    }

    @Override
    public String encode(CharSequence senha) {
        return bcrypt.encode(sha1Hex(senha));
    }

    @Override
    public boolean matches(CharSequence senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        return bcrypt.matches(sha1Hex(senha), hash);
    }

    /** Este formato só existe para ser substituído: todo hash dele precisa de upgrade. */
    @Override
    public boolean upgradeEncoding(String hash) {
        return true;
    }

    static String sha1Hex(CharSequence senha) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest(senha.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Toda JVM tem SHA-1", e);
        }
    }
}
