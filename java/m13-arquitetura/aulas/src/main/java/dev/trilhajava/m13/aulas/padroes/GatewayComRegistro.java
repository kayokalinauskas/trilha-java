package dev.trilhajava.m13.aulas.padroes;

import java.util.function.Consumer;

/**
 * Outro decorator: registra cada chamada e o desfecho. Como os dois decorators têm a
 * mesma interface, eles se empilham em qualquer ordem, e a ordem muda o que é registrado.
 */
public class GatewayComRegistro implements GatewayDePagamento {

    private final GatewayDePagamento original;
    private final Consumer<String> registro;

    public GatewayComRegistro(GatewayDePagamento original, Consumer<String> registro) {
        this.original = original;
        this.registro = registro;
    }

    @Override
    public ResultadoDaCobranca cobrar(SolicitacaoDeCobranca solicitacao) {
        registro.accept("cobrando pedido " + solicitacao.pedidoId() + " (R$ " + solicitacao.valor() + ")");
        try {
            ResultadoDaCobranca resultado = original.cobrar(solicitacao);
            registro.accept("pedido " + solicitacao.pedidoId() + " -> " + resultado);
            return resultado;
        } catch (RuntimeException e) {
            registro.accept("pedido " + solicitacao.pedidoId() + " falhou: " + e.getMessage());
            throw e;
        }
    }
}
