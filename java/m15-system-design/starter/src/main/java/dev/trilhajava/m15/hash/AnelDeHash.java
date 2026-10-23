package dev.trilhajava.m15.hash;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Hash consistente com nós virtuais. Os requisitos estão no enunciado do exercício 15.03.
 */
public class AnelDeHash {

    public AnelDeHash(int virtuaisPorNo) {
        // TODO: exercício 15.03
        throw new UnsupportedOperationException("TODO: exercício 15.03");
    }

    public void adicionar(String no) {
        throw new UnsupportedOperationException("TODO: exercício 15.03");
    }

    public void remover(String no) {
        throw new UnsupportedOperationException("TODO: exercício 15.03");
    }

    /** O nó responsável pela chave. */
    public String noPara(String chave) {
        throw new UnsupportedOperationException("TODO: exercício 15.03");
    }

    public Set<String> nos() {
        throw new UnsupportedOperationException("TODO: exercício 15.03");
    }

    /** Pronta: os 8 primeiros bytes do MD5 (espalha bem os valores e é igual em qualquer JVM). */
    static long hash(String valor) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(valor.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(md5).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
