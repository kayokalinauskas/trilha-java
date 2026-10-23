package dev.trilhajava.m16;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * Um EmbeddingModel de brinquedo para os testes: cada palavra relevante vira uma dimensão, e o vetor conta
 * quantas vezes ela aparece (um "saco de palavras"). Não entende sinônimos, como um modelo real entenderia,
 * mas é determinístico e deixa claro por que um trecho ficou mais perto da pergunta do que outro.
 */
public class ModeloDeEmbeddingFalso implements EmbeddingModel {

    public static final int DIMENSOES = 512;
    private static final Set<String> IGNORADAS = Set.of(
            "que", "com", "por", "para", "como", "dos", "das", "uma", "nos", "nas", "qual", "sao", "the", "and");

    private final Map<String, Integer> vocabulario = new HashMap<>();
    private int chamadas;
    private int textosRecebidos;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        chamadas++;
        List<Embedding> resultado = new ArrayList<>();
        List<String> textos = request.getInstructions();
        for (int i = 0; i < textos.size(); i++) {
            textosRecebidos++;
            resultado.add(new Embedding(vetor(textos.get(i)), i));
        }
        return new EmbeddingResponse(resultado);
    }

    @Override
    public float[] embed(Document document) {
        return vetor(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSOES;
    }

    public int chamadas() {
        return chamadas;
    }

    public int textosRecebidos() {
        return textosRecebidos;
    }

    private float[] vetor(String texto) {
        float[] vetor = new float[DIMENSOES];
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        for (String palavra : normalizado.split("[^a-z0-9]+")) {
            if (palavra.length() < 3 || IGNORADAS.contains(palavra)) {
                continue;
            }
            int dimensao = vocabulario.computeIfAbsent(palavra, p -> vocabulario.size() % DIMENSOES);
            vetor[dimensao]++;
        }
        return vetor;
    }
}
