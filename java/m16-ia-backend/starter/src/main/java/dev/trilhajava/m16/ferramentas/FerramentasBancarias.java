package dev.trilhajava.m16.ferramentas;

import java.math.BigDecimal;
import org.springframework.ai.chat.model.ToolContext;

/**
 * As ferramentas que o modelo pode pedir para executar. Falta transformar os métodos em ferramentas
 * (@Tool, @ToolParam) e implementar as regras do enunciado do exercício 16.03.
 */
public class FerramentasBancarias {

    public static final String CLIENTE = "cliente";
    public static final BigDecimal LIMITE_POR_PIX = new BigDecimal("5000.00");

    private final Contas contas;

    public FerramentasBancarias(Contas contas) {
        this.contas = contas;
    }

    // TODO: exercício 16.03 — ferramenta
    public String consultarSaldo(ToolContext contexto) {
        throw new UnsupportedOperationException("TODO: exercício 16.03");
    }

    // TODO: exercício 16.03 — ferramenta, com a descrição de cada parâmetro
    public String proporPix(String chave, BigDecimal valor, ToolContext contexto) {
        throw new UnsupportedOperationException("TODO: exercício 16.03");
    }
}
