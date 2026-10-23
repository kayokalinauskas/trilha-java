package dev.trilhajava.m13.aulas.solid;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: fechar o pedido. Orquestra; não calcula desconto, não sabe SQL, não monta e-mail.
 * Cada dependência chega pelo construtor e é uma abstração definida neste pacote.
 */
@Service
public class FechamentoDePedido {

    private final CalculadoraDeDescontos calculadora;
    private final RepositorioDePedidos pedidos;
    private final Notificador notificador;
    private final Clock relogio;

    public FechamentoDePedido(CalculadoraDeDescontos calculadora, RepositorioDePedidos pedidos,
                              Notificador notificador, Clock relogio) {
        this.calculadora = calculadora;
        this.pedidos = pedidos;
        this.notificador = notificador;
        this.relogio = relogio;
    }

    public PedidoFechado fechar(String numero, Carrinho carrinho) {
        Orcamento orcamento = calculadora.orcar(carrinho);
        var pedido = new PedidoFechado(numero, carrinho.clienteId(), orcamento, Instant.now(relogio));
        pedidos.salvar(pedido);
        notificador.pedidoFechado(pedido);
        return pedido;
    }
}
