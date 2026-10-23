package dev.trilhajava.m11.aulas.dados;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Um KMS de mentira, para testes e para rodar a aula sem nuvem. Embrulha cada DEK com uma chave
 * mestra (KEK) usando AES-GCM. Num KMS de verdade, a KEK mora num HSM e nunca é exportável: este
 * objeto existe só para mostrar o fluxo do envelope.
 */
public final class KmsEmMemoria implements ServicoDeChaves {

    private static final byte[] AAD = "trilhajava:dek".getBytes(StandardCharsets.UTF_8);

    private final SecretKey chaveMestra;
    private final SecureRandom aleatorio = new SecureRandom();

    public KmsEmMemoria() {
        this.chaveMestra = novaChaveAes();
    }

    @Override
    public ChaveDeDadosGerada gerarChaveDeDados() {
        SecretKey dek = novaChaveAes();
        byte[] nonce = new byte[12];
        aleatorio.nextBytes(nonce);
        try {
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.ENCRYPT_MODE, chaveMestra, new GCMParameterSpec(128, nonce));
            cifra.updateAAD(AAD);
            byte[] cifrada = cifra.doFinal(dek.getEncoded());
            byte[] embrulhada = ByteBuffer.allocate(nonce.length + cifrada.length).put(nonce).put(cifrada).array();
            return new ChaveDeDadosGerada(dek, embrulhada);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("falha ao embrulhar a chave de dados", e);
        }
    }

    @Override
    public SecretKey desembrulhar(byte[] embrulhada) {
        try {
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.DECRYPT_MODE, chaveMestra, new GCMParameterSpec(128, embrulhada, 0, 12));
            cifra.updateAAD(AAD);
            byte[] dek = cifra.doFinal(embrulhada, 12, embrulhada.length - 12);
            return new SecretKeySpec(dek, "AES");
        } catch (GeneralSecurityException e) {
            throw new FalhaAoDecifrarException("a chave de dados não pôde ser desembrulhada", e);
        }
    }

    private static SecretKey novaChaveAes() {
        try {
            KeyGenerator gerador = KeyGenerator.getInstance("AES");
            gerador.init(256);
            return gerador.generateKey();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-256 indisponível nesta JVM", e);
        }
    }
}
