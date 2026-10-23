package dev.trilhajava.m15.hash;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Hash consistente: os nós e as chaves são posicionados no mesmo anel (o espaço de valores de um long),
 * e cada chave pertence ao primeiro nó no sentido horário. Adicionar ou remover um nó só move as chaves
 * do trecho vizinho, e não quase todas, como no {@code hash(chave) % n}.
 *
 * <p>Cada nó físico aparece no anel várias vezes (nós virtuais). Com um ponto só por nó, os trechos
 * ficam desiguais e um nó recebe muito mais chaves que outro.
 *
 * <p>Não é thread-safe: em um sistema real, a topologia muda raramente e é trocada inteira (cópia nova).
 */
public class AnelDeHash {

    private final int virtuaisPorNo;
    private final NavigableMap<Long, String> anel = new TreeMap<>();
    private final Set<String> nos = new TreeSet<>();

    public AnelDeHash(int virtuaisPorNo) {
        if (virtuaisPorNo < 1) {
            throw new IllegalArgumentException("cada nó precisa de pelo menos um ponto no anel");
        }
        this.virtuaisPorNo = virtuaisPorNo;
    }

    public void adicionar(String no) {
        if (!nos.add(no)) {
            return;
        }
        for (int i = 0; i < virtuaisPorNo; i++) {
            anel.put(hash(no + "#" + i), no);
        }
    }

    public void remover(String no) {
        if (!nos.remove(no)) {
            return;
        }
        for (int i = 0; i < virtuaisPorNo; i++) {
            anel.remove(hash(no + "#" + i), no);
        }
    }

    /** O nó responsável pela chave: o primeiro ponto do anel a partir do hash da chave (dando a volta). */
    public String noPara(String chave) {
        if (anel.isEmpty()) {
            throw new IllegalStateException("anel sem nós");
        }
        Map.Entry<Long, String> ponto = anel.ceilingEntry(hash(chave));
        return (ponto != null ? ponto : anel.firstEntry()).getValue();
    }

    public Set<String> nos() {
        return Set.copyOf(nos);
    }

    /**
     * Os 8 primeiros bytes do MD5. Não é por segurança: o MD5 espalha bem os valores e é igual em qualquer
     * JVM (o String.hashCode concentra chaves parecidas, como "no-1#0" e "no-1#1", perto umas das outras).
     */
    static long hash(String valor) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(valor.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(md5).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
