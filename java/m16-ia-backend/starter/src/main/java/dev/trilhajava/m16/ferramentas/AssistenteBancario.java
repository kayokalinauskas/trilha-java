package dev.trilhajava.m16.ferramentas;

import org.springframework.ai.chat.model.ChatModel;

/** O assistente do app: conversa com o cliente autenticado e pode usar as ferramentas bancárias. */
public class AssistenteBancario {

    public AssistenteBancario(ChatModel modelo, FerramentasBancarias ferramentas) {
        // TODO: exercício 16.03
    }

    /** {@code cliente} vem da autenticação (o JWT, a sessão), nunca do texto da conversa. */
    public String responder(String cliente, String pergunta) {
        throw new UnsupportedOperationException("TODO: exercício 16.03");
    }
}
