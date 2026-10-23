package dev.trilhajava.m13.aulas.solid;

import java.util.Optional;

/** Porta de saída: o caso de uso diz do que precisa; o adaptador (JDBC, JPA, memória) implementa. */
public interface RepositorioDePedidos {

    void salvar(PedidoFechado pedido);

    /** Vazio quando o número não existe; nunca lança exceção por "não encontrado". */
    Optional<PedidoFechado> buscar(String numero);
}
