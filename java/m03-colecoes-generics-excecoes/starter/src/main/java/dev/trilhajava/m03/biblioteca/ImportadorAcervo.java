package dev.trilhajava.m03.biblioteca;

import java.io.Reader;
import java.nio.file.Path;

/**
 * Importa um CSV "isbn;titulo;exemplares" (com cabeçalho) para a biblioteca.
 * Tudo ou nada: se qualquer linha for inválida, nenhum livro é cadastrado.
 */
public class ImportadorAcervo {

    public ImportadorAcervo(Biblioteca biblioteca) {
        throw new UnsupportedOperationException("TODO");
    }

    public int importar(Reader leitor) throws ArquivoAcervoInvalidoException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Dica: try-with-resources para abrir o arquivo; IOException vira ArquivoAcervoInvalidoException (com a causa!). */
    public int importar(Path arquivo) throws ArquivoAcervoInvalidoException {
        throw new UnsupportedOperationException("TODO");
    }
}
