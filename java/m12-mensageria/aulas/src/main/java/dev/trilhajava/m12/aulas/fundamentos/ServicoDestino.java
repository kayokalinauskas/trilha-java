package dev.trilhajava.m12.aulas.fundamentos;

/** Quem reage a um pedido pago: nota fiscal, estoque, e-mail, fidelidade... */
public interface ServicoDestino {

    String nome();

    /** Faz o trabalho; lança {@link ServicoIndisponivelException} se estiver fora do ar. */
    void processar(PedidoPago pago);
}
