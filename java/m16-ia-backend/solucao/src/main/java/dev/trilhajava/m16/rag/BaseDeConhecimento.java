package dev.trilhajava.m16.rag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * A parte "R" do RAG, feita à mão: divide documentos em trechos, guarda o embedding de cada um e busca os
 * trechos mais parecidos com a pergunta, sempre dentro de um único cliente (tenant).
 */
public class BaseDeConhecimento {

    private record Chave(String tenant, String documentoId) {
    }

    private record Indexado(String documentoId, int indice, String texto, float[] vetor) {
    }

    private static final Comparator<Trecho> MAIS_PARECIDO_PRIMEIRO = Comparator
            .comparingDouble(Trecho::similaridade).reversed()
            .thenComparing(Trecho::documentoId)
            .thenComparingInt(Trecho::indice);

    private final EmbeddingModel modelo;
    private final int palavrasPorTrecho;
    private final int sobreposicao;
    private final Map<Chave, List<Indexado>> documentos = new HashMap<>();

    public BaseDeConhecimento(EmbeddingModel modelo, int palavrasPorTrecho, int sobreposicao) {
        validarDivisao(palavrasPorTrecho, sobreposicao);
        this.modelo = modelo;
        this.palavrasPorTrecho = palavrasPorTrecho;
        this.sobreposicao = sobreposicao;
    }

    /** Janelas de {@code palavras} palavras que andam {@code palavras - sobreposicao} por vez. */
    public static List<String> dividir(String texto, int palavras, int sobreposicao) {
        validarDivisao(palavras, sobreposicao);
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        String[] todas = texto.strip().split("\\s+");
        int passo = palavras - sobreposicao;
        List<String> trechos = new ArrayList<>();
        for (int inicio = 0; ; inicio += passo) {
            int fim = Math.min(inicio + palavras, todas.length);
            trechos.add(String.join(" ", Arrays.copyOfRange(todas, inicio, fim)));
            if (fim == todas.length) {
                return trechos;
            }
        }
    }

    public static double cosseno(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("dimensões diferentes: " + a.length + " e " + b.length);
        }
        double produto = 0;
        double normaA = 0;
        double normaB = 0;
        for (int i = 0; i < a.length; i++) {
            produto += (double) a[i] * b[i];
            normaA += (double) a[i] * a[i];
            normaB += (double) b[i] * b[i];
        }
        if (normaA == 0 || normaB == 0) {
            return 0.0;
        }
        return produto / (Math.sqrt(normaA) * Math.sqrt(normaB));
    }

    /** Indexa (ou reindexa, substituindo a versão anterior) um documento do tenant. */
    public void indexar(String tenant, String documentoId, String texto) {
        exigir(tenant, "tenant");
        exigir(documentoId, "documentoId");
        List<String> trechos = dividir(texto, palavrasPorTrecho, sobreposicao);
        var chave = new Chave(tenant, documentoId);
        if (trechos.isEmpty()) {
            documentos.remove(chave);
            return;
        }
        List<float[]> vetores = modelo.embed(trechos); // uma chamada para o documento inteiro, em lote
        List<Indexado> indexados = new ArrayList<>();
        for (int i = 0; i < trechos.size(); i++) {
            indexados.add(new Indexado(documentoId, i, trechos.get(i), vetores.get(i)));
        }
        documentos.put(chave, List.copyOf(indexados));
    }

    public List<Trecho> buscar(String tenant, String pergunta, int topK, double limiarMinimo) {
        exigir(tenant, "tenant");
        exigir(pergunta, "pergunta");
        if (topK <= 0) {
            throw new IllegalArgumentException("topK precisa ser positivo");
        }
        float[] consulta = modelo.embed(pergunta);
        return documentos.entrySet().stream()
                .filter(e -> e.getKey().tenant().equals(tenant))
                .flatMap(e -> e.getValue().stream())
                .map(i -> new Trecho(i.documentoId(), i.indice(), i.texto(), cosseno(consulta, i.vetor())))
                .filter(t -> t.similaridade() >= limiarMinimo)
                .sorted(MAIS_PARECIDO_PRIMEIRO)
                .limit(topK)
                .toList();
    }

    public void remover(String tenant, String documentoId) {
        documentos.remove(new Chave(tenant, documentoId));
    }

    public int totalDeTrechos() {
        return documentos.values().stream().mapToInt(List::size).sum();
    }

    private static void validarDivisao(int palavras, int sobreposicao) {
        if (palavras <= 0) {
            throw new IllegalArgumentException("cada trecho precisa de pelo menos uma palavra");
        }
        if (sobreposicao < 0 || sobreposicao >= palavras) {
            throw new IllegalArgumentException("a sobreposição precisa estar entre 0 e " + (palavras - 1));
        }
    }

    private static void exigir(String valor, String nome) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nome + " é obrigatório");
        }
    }
}
