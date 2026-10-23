package dev.trilhajava.m11.aulas.owasp.arquivos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * VULNERÁVEL DE PROPÓSITO (A01, CWE-22 path traversal). Não copie.
 * GET /notas/{arquivo} devolve o PDF da nota fiscal guardado na pasta de notas.
 */
public class NotasFiscaisVulneravel {

    private final Path pasta;

    public NotasFiscaisVulneravel(Path pasta) {
        this.pasta = pasta;
    }

    public byte[] baixar(String arquivo) throws IOException {
        return Files.readAllBytes(pasta.resolve(arquivo));
    }
}
