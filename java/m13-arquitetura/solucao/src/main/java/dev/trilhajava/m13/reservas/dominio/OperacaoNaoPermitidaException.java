package dev.trilhajava.m13.reservas.dominio;

/** Quem pediu não tem direito a esta operação (ex.: cancelar a reserva de outra pessoa). */
public class OperacaoNaoPermitidaException extends RuntimeException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
