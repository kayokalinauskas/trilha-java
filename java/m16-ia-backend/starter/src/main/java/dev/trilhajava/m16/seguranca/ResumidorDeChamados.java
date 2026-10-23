package dev.trilhajava.m16.seguranca;

import org.springframework.ai.chat.model.ChatModel;

/**
 * Resume chamados de clientes para o time de atendimento, tratando o texto do chamado como dado não
 * confiável. As defesas exigidas estão no enunciado do exercício 16.04.
 */
public class ResumidorDeChamados {

    public static final int TAMANHO_MAXIMO = 4000;
    public static final String RESPOSTA_BLOQUEADA = "Resumo indisponível: a resposta do modelo foi bloqueada pela verificação de segurança.";

    public ResumidorDeChamados(ChatModel modelo) {
        // TODO: exercício 16.04 — gere o canário e monte o ChatClient com as instruções de sistema
    }

    public String resumir(String chamado) {
        throw new UnsupportedOperationException("TODO: exercício 16.04");
    }
}
