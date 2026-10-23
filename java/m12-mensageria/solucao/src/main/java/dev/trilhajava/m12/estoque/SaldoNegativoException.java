package dev.trilhajava.m12.estoque;

/** Falha permanente: reprocessar a mesma mensagem nunca vai dar certo. */
public class SaldoNegativoException extends RuntimeException {

    public SaldoNegativoException(String sku, int saldo, int quantidade) {
        super("saldo de %s ficaria negativo: %d %+d".formatted(sku, saldo, quantidade));
    }
}
