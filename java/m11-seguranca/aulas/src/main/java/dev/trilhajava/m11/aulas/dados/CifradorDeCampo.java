package dev.trilhajava.m11.aulas.dados;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Cifra um campo com AES-256-GCM e devolve um texto pronto para gravar numa coluna:
 *
 * <pre>
 *   v2:BASE64( nonce de 12 bytes | texto cifrado | tag de 16 bytes )
 * </pre>
 *
 * <ul>
 *   <li>O prefixo diz com qual versão de chave o valor foi cifrado; é ele que permite rotacionar.</li>
 *   <li>O nonce é aleatório e novo a cada chamada: o mesmo CPF gera um valor diferente a cada vez.</li>
 *   <li>O contexto (ex. {@code "paciente.cpf"}) e a versão entram como AAD: não são cifrados, mas são
 *       autenticados. Um valor copiado para outro campo, ou com o prefixo trocado, não abre.</li>
 * </ul>
 */
public final class CifradorDeCampo {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANHO_NONCE = 12;     // 96 bits, o recomendado para GCM
    private static final int TAMANHO_TAG_BITS = 128;  // tag completa; não encurte

    private final ChaveiroDeDados chaveiro;
    private final SecureRandom aleatorio = new SecureRandom();

    public CifradorDeCampo(ChaveiroDeDados chaveiro) {
        this.chaveiro = chaveiro;
    }

    public String cifrar(String textoClaro, String contexto) {
        int versao = chaveiro.versaoAtual();
        byte[] nonce = new byte[TAMANHO_NONCE];
        aleatorio.nextBytes(nonce);
        try {
            Cipher cifra = Cipher.getInstance(ALGORITMO);
            cifra.init(Cipher.ENCRYPT_MODE, chaveiro.atual(), new GCMParameterSpec(TAMANHO_TAG_BITS, nonce));
            cifra.updateAAD(aad(contexto, versao));
            byte[] cifrado = cifra.doFinal(textoClaro.getBytes(StandardCharsets.UTF_8));
            byte[] pacote = ByteBuffer.allocate(nonce.length + cifrado.length).put(nonce).put(cifrado).array();
            return "v" + versao + ":" + Base64.getEncoder().encodeToString(pacote);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("falha ao cifrar " + contexto, e);
        }
    }

    public String decifrar(String armazenado, String contexto) {
        int versao = versaoDe(armazenado);
        byte[] pacote = Base64.getDecoder().decode(armazenado.substring(armazenado.indexOf(':') + 1));
        if (pacote.length < TAMANHO_NONCE + TAMANHO_TAG_BITS / 8) {
            throw new FalhaAoDecifrarException("valor cifrado truncado em " + contexto, null);
        }
        try {
            Cipher cifra = Cipher.getInstance(ALGORITMO);
            cifra.init(Cipher.DECRYPT_MODE, chaveiro.versao(versao),
                    new GCMParameterSpec(TAMANHO_TAG_BITS, pacote, 0, TAMANHO_NONCE));
            cifra.updateAAD(aad(contexto, versao));
            byte[] claro = cifra.doFinal(pacote, TAMANHO_NONCE, pacote.length - TAMANHO_NONCE);
            return new String(claro, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new FalhaAoDecifrarException("não foi possível decifrar " + contexto, e);
        }
    }

    /** O prefixo dos valores cifrados com a chave atual, ex. {@code "v2:"}. */
    public String prefixoAtual() {
        return "v" + chaveiro.versaoAtual() + ":";
    }

    /** O valor foi cifrado com uma versão que não é a atual e deve ser regravado. */
    public boolean precisaRecifrar(String armazenado) {
        return versaoDe(armazenado) != chaveiro.versaoAtual();
    }

    /** Abre com a chave antiga (a do prefixo) e fecha com a atual. */
    public String recifrar(String armazenado, String contexto) {
        return cifrar(decifrar(armazenado, contexto), contexto);
    }

    static int versaoDe(String armazenado) {
        int doisPontos = armazenado.indexOf(':');
        if (!armazenado.startsWith("v") || doisPontos < 2) {
            throw new FalhaAoDecifrarException("formato desconhecido: falta o prefixo de versão", null);
        }
        try {
            return Integer.parseInt(armazenado.substring(1, doisPontos));
        } catch (NumberFormatException e) {
            throw new FalhaAoDecifrarException("prefixo de versão inválido", e);
        }
    }

    private static byte[] aad(String contexto, int versao) {
        return (contexto + "|v" + versao).getBytes(StandardCharsets.UTF_8);
    }
}
