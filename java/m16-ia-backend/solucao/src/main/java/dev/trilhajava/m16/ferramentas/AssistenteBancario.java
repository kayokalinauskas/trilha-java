package dev.trilhajava.m16.ferramentas;

import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

/** O assistente do app: conversa com o cliente autenticado e pode usar as ferramentas bancárias. */
public class AssistenteBancario {

    static final String INSTRUCOES = """
            Você é o assistente do app do banco. Responda em português, de forma curta.
            Use as ferramentas para consultar o saldo ou preparar um Pix; nunca invente valores.
            Um Pix preparado por você fica pendente até o cliente confirmar no app: diga isso a ele.
            """;

    private final ChatClient chat;
    private final FerramentasBancarias ferramentas;

    public AssistenteBancario(ChatModel modelo, FerramentasBancarias ferramentas) {
        this.chat = ChatClient.builder(modelo).defaultSystem(INSTRUCOES).build();
        this.ferramentas = ferramentas;
    }

    /** {@code cliente} vem da autenticação (o JWT, a sessão), nunca do texto da conversa. */
    public String responder(String cliente, String pergunta) {
        if (cliente == null || cliente.isBlank()) {
            throw new SecurityException("nenhum cliente autenticado");
        }
        return chat.prompt()
                .user(pergunta)
                .tools(ferramentas)
                .toolContext(Map.of(FerramentasBancarias.CLIENTE, cliente))
                .call()
                .content();
    }
}
