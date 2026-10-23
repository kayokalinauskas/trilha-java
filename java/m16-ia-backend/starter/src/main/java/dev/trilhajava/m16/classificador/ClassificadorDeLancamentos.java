package dev.trilhajava.m16.classificador;

import org.springframework.ai.chat.model.ChatModel;

/**
 * Classifica lançamentos do extrato com um LLM. As regras (saída estruturada, temperatura, delimitadores,
 * validação, nova tentativa e fallback) estão no enunciado do exercício 16.01.
 */
public class ClassificadorDeLancamentos {

    public static final double CONFIANCA_MINIMA = 0.6;
    public static final int TAMANHO_MAXIMO = 200;

    public ClassificadorDeLancamentos(ChatModel modelo) {
        // TODO: exercício 16.01 — monte o ChatClient com as instruções de sistema e a temperatura
    }

    public Classificacao classificar(String descricao) {
        throw new UnsupportedOperationException("TODO: exercício 16.01");
    }
}
