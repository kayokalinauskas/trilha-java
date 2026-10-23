package dev.trilhajava.m16.rag;

import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * A parte "R" do RAG, feita à mão: trechos, embeddings, cosseno e isolamento por cliente (tenant).
 * Os requisitos estão no enunciado do exercício 16.02.
 */
public class BaseDeConhecimento {

    public BaseDeConhecimento(EmbeddingModel modelo, int palavrasPorTrecho, int sobreposicao) {
        // TODO: exercício 16.02
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }

    /** Janelas de {@code palavras} palavras que andam {@code palavras - sobreposicao} por vez. */
    public static List<String> dividir(String texto, int palavras, int sobreposicao) {
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }

    public static double cosseno(float[] a, float[] b) {
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }

    /** Indexa (ou reindexa, substituindo a versão anterior) um documento do tenant. */
    public void indexar(String tenant, String documentoId, String texto) {
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }

    public List<Trecho> buscar(String tenant, String pergunta, int topK, double limiarMinimo) {
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }

    public void remover(String tenant, String documentoId) {
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }

    public int totalDeTrechos() {
        throw new UnsupportedOperationException("TODO: exercício 16.02");
    }
}
