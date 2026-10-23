package dev.trilhajava.m03.biblioteca;

/**
 * Checked de propósito: arquivo ruim é uma situação ESPERADA e recuperável
 * (o operador pode corrigir e reenviar), então o compilador obriga quem chama a tratar.
 */
public class ArquivoAcervoInvalidoException extends Exception {

    private final int linha;

    public ArquivoAcervoInvalidoException(String mensagem, int linha, Throwable causa) {
        super(mensagem, causa);
        this.linha = linha;
    }

    public int linha() {
        return linha;
    }
}
