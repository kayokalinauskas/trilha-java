package dev.trilhajava.m16;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * Um ChatModel roteirizado para os testes: cada chamada consome o próximo passo do roteiro, e todo prompt
 * recebido fica guardado para inspeção. Sem rede, sem chave de API e com respostas previsíveis.
 */
public class ModeloDeChatFalso implements ChatModel {

    private final Deque<Function<Prompt, AssistantMessage>> roteiro = new ArrayDeque<>();
    private final List<Prompt> recebidos = new ArrayList<>();

    /** Responde com estes textos, um por chamada, na ordem. */
    public ModeloDeChatFalso responde(String... textos) {
        for (String texto : textos) {
            roteiro.add(prompt -> new AssistantMessage(texto));
        }
        return this;
    }

    /** Monta a resposta a partir do prompt recebido (para respostas que dependem do que foi enviado). */
    public ModeloDeChatFalso respondeCom(Function<Prompt, String> resposta) {
        roteiro.add(prompt -> new AssistantMessage(resposta.apply(prompt)));
        return this;
    }

    /** Na próxima chamada, o "modelo" pede para executar esta ferramenta com estes argumentos (JSON). */
    public ModeloDeChatFalso chamaFerramenta(String nome, String argumentosJson) {
        roteiro.add(prompt -> AssistantMessage.builder()
                .toolCalls(List.of(new AssistantMessage.ToolCall("chamada-" + recebidos.size(), "function", nome, argumentosJson)))
                .build());
        return this;
    }

    /** Na próxima chamada, o provedor falha (timeout, 429, 500...). */
    public ModeloDeChatFalso falha(RuntimeException erro) {
        roteiro.add(prompt -> {
            throw erro;
        });
        return this;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        recebidos.add(prompt);
        if (roteiro.isEmpty()) {
            throw new IllegalStateException("o modelo falso foi chamado mais vezes do que o roteiro previa");
        }
        return new ChatResponse(List.of(new Generation(roteiro.pop().apply(prompt))));
    }

    /**
     * Sem isto, o ChatClient ignora em silêncio as ferramentas passadas com .tools(): o laço de tool calling
     * só roda quando as opções do modelo são ToolCallingChatOptions (é o que os modelos reais devolvem).
     */
    @Override
    public ChatOptions getOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    public List<Prompt> recebidos() {
        return recebidos;
    }

    public Prompt ultimo() {
        return recebidos.getLast();
    }

    public int chamadas() {
        return recebidos.size();
    }
}
