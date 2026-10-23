package dev.trilhajava.m15.saga;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Saga orquestrada de uma transferência para outro banco: debitar aqui, enviar ao parceiro e, se preciso,
 * compensar. As regras (inclusive o que fazer quando o parceiro não responde) estão no enunciado do 15.04.
 */
public class OrquestradorDeTransferencias {

    private final Contas contas;
    private final BancoParceiro parceiro;

    public OrquestradorDeTransferencias(Contas contas, BancoParceiro parceiro) {
        this.contas = contas;
        this.parceiro = parceiro;
    }

    public Optional<Transferencia> buscar(String id) {
        throw new UnsupportedOperationException("TODO: exercício 15.04");
    }

    public Transferencia iniciar(String id, String origem, String contaDestino, BigDecimal valor) {
        throw new UnsupportedOperationException("TODO: exercício 15.04");
    }

    /** Resolve uma transferência PENDENTE perguntando ao parceiro. */
    public Transferencia reconciliar(String id) {
        throw new UnsupportedOperationException("TODO: exercício 15.04");
    }
}
