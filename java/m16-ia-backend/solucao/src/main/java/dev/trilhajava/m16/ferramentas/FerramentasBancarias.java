package dev.trilhajava.m16.ferramentas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * As ferramentas que o modelo pode pedir para executar. O cliente NUNCA é um argumento: ele vem do
 * ToolContext, que a aplicação preenche com o usuário autenticado e o modelo não consegue ver nem mudar.
 */
public class FerramentasBancarias {

    public static final String CLIENTE = "cliente";
    public static final BigDecimal LIMITE_POR_PIX = new BigDecimal("5000.00");

    private final Contas contas;

    public FerramentasBancarias(Contas contas) {
        this.contas = contas;
    }

    @Tool(description = "Consulta o saldo disponível na conta do cliente que está conversando com o assistente")
    public String consultarSaldo(ToolContext contexto) {
        String cliente = clienteAutenticado(contexto);
        return "Saldo disponível: R$ " + saldoDe(cliente).setScale(2, RoundingMode.HALF_EVEN).toPlainString();
    }

    @Tool(description = """
            Prepara um Pix para o cliente revisar e confirmar no app. NÃO transfere dinheiro: \
            devolve o id de uma proposta pendente.""")
    public String proporPix(
            @ToolParam(description = "Chave Pix do destinatário (CPF, e-mail, telefone ou chave aleatória)") String chave,
            @ToolParam(description = "Valor em reais, com ponto decimal. Ex.: 150.00") BigDecimal valor,
            ToolContext contexto) {
        String cliente = clienteAutenticado(contexto);
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("informe a chave Pix do destinatário");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("o valor precisa ser maior que zero");
        }
        if (valor.compareTo(LIMITE_POR_PIX) > 0) {
            throw new IllegalArgumentException("valor acima do limite de R$ 5000.00 por Pix pelo assistente");
        }
        if (valor.compareTo(saldoDe(cliente)) > 0) {
            throw new IllegalArgumentException("saldo insuficiente para este Pix");
        }
        var proposta = contas.registrarProposta(cliente, chave.strip(), valor.setScale(2, RoundingMode.HALF_EVEN));
        return "Proposta " + proposta.id() + " criada: R$ " + proposta.valor().toPlainString() + " para " + proposta.chave()
                + ". O cliente precisa confirmar no app para o Pix sair.";
    }

    private BigDecimal saldoDe(String cliente) {
        return contas.saldo(cliente).orElseThrow(() -> new IllegalStateException("conta não encontrada"));
    }

    static String clienteAutenticado(ToolContext contexto) {
        Object cliente = contexto == null ? null : contexto.getContext().get(CLIENTE);
        if (!(cliente instanceof String c) || c.isBlank()) {
            throw new SecurityException("nenhum cliente autenticado na conversa");
        }
        return c;
    }
}
