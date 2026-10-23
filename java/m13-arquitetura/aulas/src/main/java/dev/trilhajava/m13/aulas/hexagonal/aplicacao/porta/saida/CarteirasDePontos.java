package dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida;

import dev.trilhajava.m13.aulas.hexagonal.dominio.CarteiraDePontos;
import java.util.Optional;

/** Porta de saída: onde as carteiras ficam guardadas. O núcleo define; um adaptador implementa. */
public interface CarteirasDePontos {

    Optional<CarteiraDePontos> doCliente(String clienteId);

    /**
     * Grava o novo saldo.
     *
     * @throws CarteiraAlteradaException se outra operação gravou a mesma carteira depois da leitura
     */
    void salvar(CarteiraDePontos carteira);
}
