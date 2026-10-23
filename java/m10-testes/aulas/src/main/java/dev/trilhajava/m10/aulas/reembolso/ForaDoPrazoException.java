package dev.trilhajava.m10.aulas.reembolso;

/** O pedido foi entregue há mais de 7 dias: o prazo de arrependimento acabou. */
public class ForaDoPrazoException extends RuntimeException {

    public ForaDoPrazoException(String mensagem) {
        super(mensagem);
    }
}
