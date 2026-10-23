package dev.trilhajava.m11.aulas.dados;

import java.util.Map;
import java.util.TreeMap;
import javax.crypto.SecretKey;

/**
 * As chaves de dados (DEKs) da aplicação, cada uma com um número de versão.
 *
 * <p>Só a versão atual cifra; todas as versões presentes decifram. Rotacionar é acrescentar uma
 * versão nova, torná-la a atual e, com o tempo, regravar os dados antigos com ela. Uma versão só
 * sai do chaveiro quando nenhum dado (nem backup que você ainda precise ler) depende dela.
 */
public final class ChaveiroDeDados {

    private final Map<Integer, SecretKey> chaves;
    private final int versaoAtual;

    public ChaveiroDeDados(Map<Integer, SecretKey> chaves, int versaoAtual) {
        if (!chaves.containsKey(versaoAtual)) {
            throw new IllegalArgumentException("a versão atual " + versaoAtual + " não está no chaveiro");
        }
        chaves.forEach((versao, chave) -> {
            if (chave.getEncoded().length != 32) {
                throw new IllegalArgumentException("a chave v" + versao + " não tem 256 bits");
            }
        });
        this.chaves = Map.copyOf(new TreeMap<>(chaves));
        this.versaoAtual = versaoAtual;
    }

    /**
     * Monta o chaveiro a partir das DEKs embrulhadas (envelope encryption): só o KMS sabe abri-las,
     * e a aplicação guarda em memória apenas as chaves já desembrulhadas.
     */
    public static ChaveiroDeDados carregar(ServicoDeChaves kms, Map<Integer, byte[]> embrulhadas, int versaoAtual) {
        Map<Integer, SecretKey> chaves = new TreeMap<>();
        embrulhadas.forEach((versao, embrulhada) -> chaves.put(versao, kms.desembrulhar(embrulhada)));
        return new ChaveiroDeDados(chaves, versaoAtual);
    }

    public int versaoAtual() {
        return versaoAtual;
    }

    public SecretKey atual() {
        return chaves.get(versaoAtual);
    }

    public SecretKey versao(int versao) {
        SecretKey chave = chaves.get(versao);
        if (chave == null) {
            throw new FalhaAoDecifrarException("versão de chave desconhecida: v" + versao, null);
        }
        return chave;
    }
}
