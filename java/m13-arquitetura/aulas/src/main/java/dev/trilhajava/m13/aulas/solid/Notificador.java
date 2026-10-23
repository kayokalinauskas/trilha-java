package dev.trilhajava.m13.aulas.solid;

/** Porta de saída: avisar a pessoa. E-mail, push ou fila são detalhes do adaptador. */
public interface Notificador {

    void pedidoFechado(PedidoFechado pedido);
}
