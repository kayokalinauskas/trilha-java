package dev.trilhajava.m03.biblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Solução de referência: valida tudo primeiro, cadastra depois (tudo ou nada). */
public class ImportadorAcervo {

    private final Biblioteca biblioteca;

    public ImportadorAcervo(Biblioteca biblioteca) {
        if (biblioteca == null) {
            throw new IllegalArgumentException("Biblioteca é obrigatória");
        }
        this.biblioteca = biblioteca;
    }

    public int importar(Path arquivo) throws ArquivoAcervoInvalidoException {
        // try-with-resources: o arquivo é fechado mesmo se der exceção no meio da leitura.
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            return importar(leitor);
        } catch (IOException e) {
            // Tradução de exceção: quem chama só precisa conhecer ArquivoAcervoInvalidoException,
            // mas a causa original é preservada para diagnóstico.
            throw new ArquivoAcervoInvalidoException("Não foi possível ler o arquivo " + arquivo, 0, e);
        }
    }

    public int importar(Reader leitor) throws ArquivoAcervoInvalidoException {
        List<LinhaValida> linhas = new ArrayList<>();
        BufferedReader buffer = leitor instanceof BufferedReader b ? b : new BufferedReader(leitor);
        try {
            String conteudo;
            int numero = 0;
            while ((conteudo = buffer.readLine()) != null) {
                numero++;
                if (numero == 1 || conteudo.isBlank()) {
                    continue; // cabeçalho e linhas vazias
                }
                linhas.add(interpretar(conteudo, numero));
            }
        } catch (IOException e) {
            throw new ArquivoAcervoInvalidoException("Falha de leitura do acervo", 0, e);
        }
        for (LinhaValida l : linhas) {
            biblioteca.cadastrar(l.livro(), l.exemplares());
        }
        return linhas.size();
    }

    private static LinhaValida interpretar(String conteudo, int numero) throws ArquivoAcervoInvalidoException {
        String[] colunas = conteudo.split(";", -1);
        if (colunas.length != 3) {
            throw new ArquivoAcervoInvalidoException(
                    "Linha " + numero + ": esperadas 3 colunas (isbn;titulo;exemplares), encontradas " + colunas.length, numero, null);
        }
        try {
            int exemplares = Integer.parseInt(colunas[2].strip());
            if (exemplares < 1) {
                throw new ArquivoAcervoInvalidoException("Linha " + numero + ": exemplares deve ser pelo menos 1", numero, null);
            }
            return new LinhaValida(new Livro(colunas[0], colunas[1]), exemplares);
        } catch (NumberFormatException e) {
            throw new ArquivoAcervoInvalidoException("Linha " + numero + ": exemplares não é um número: " + colunas[2], numero, e);
        } catch (IllegalArgumentException e) {
            throw new ArquivoAcervoInvalidoException("Linha " + numero + ": " + e.getMessage(), numero, e);
        }
    }

    private record LinhaValida(Livro livro, int exemplares) {
    }
}
