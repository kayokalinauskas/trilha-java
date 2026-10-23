package dev.trilhajava.m11.aulas.oidc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PKCE (RFC 7636): o cliente inventa um segredo de uso único (code_verifier), manda só o
 * hash dele (code_challenge) no pedido de autorização e revela o segredo ao trocar o código
 * por tokens. Quem interceptar o código no redirect não tem o verifier e não consegue trocá-lo.
 */
public final class Pkce {

    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    private Pkce() {
    }

    /** 32 bytes aleatórios em Base64URL sem padding: 43 caracteres, dentro do intervalo 43–128 da RFC. */
    public static String novoVerifier(SecureRandom aleatorio) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return BASE64_URL.encodeToString(bytes);
    }

    /** Método S256: BASE64URL(SHA-256(ASCII(code_verifier))). */
    public static String challengeS256(String verifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return BASE64_URL.encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Toda JVM é obrigada a ter SHA-256; se faltar, a instalação está quebrada.
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }
}
