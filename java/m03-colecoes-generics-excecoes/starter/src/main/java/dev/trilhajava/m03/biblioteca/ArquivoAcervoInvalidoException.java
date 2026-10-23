package dev.trilhajava.m03.biblioteca;

/**
 * Erro ao importar o arquivo do acervo. Diferente das outras, esta é uma exceção CHECKED:
 * quem chama o importador é obrigado a decidir o que fazer (avisar o operador, tentar outro arquivo...).
 */
public class ArquivoAcervoInvalidoException extends Exception {

    public ArquivoAcervoInvalidoException(String mensagem, int linha, Throwable causa) {
        super(mensagem, causa);
        // TODO: guarde a linha
    }

    /** Linha do arquivo onde o problema foi encontrado (0 quando o problema é o arquivo em si). */
    public int linha() {
        throw new UnsupportedOperationException("TODO");
    }
}
