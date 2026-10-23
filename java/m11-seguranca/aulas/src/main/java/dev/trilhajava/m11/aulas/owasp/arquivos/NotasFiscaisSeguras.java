package dev.trilhajava.m11.aulas.owasp.arquivos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * A versão corrigida, em três camadas:
 * 1) o nome tem um formato fechado (só o que uma nota fiscal pode ter);
 * 2) o caminho normalizado precisa continuar dentro da pasta;
 * 3) o caminho REAL (com links simbólicos resolvidos) também precisa estar dentro da pasta.
 * Melhor ainda é não aceitar nome nenhum: receber o id da nota, conferir o dono e buscar o caminho no banco.
 */
public class NotasFiscaisSeguras {

    private final Path pasta;

    public NotasFiscaisSeguras(Path pasta) throws IOException {
        this.pasta = pasta.toRealPath();
    }

    public byte[] baixar(String arquivo) throws IOException {
        if (!nomeValido(arquivo)) {
            throw new ArquivoForaDaPastaException("nome de arquivo fora do formato");
        }
        Path alvo = pasta.resolve(arquivo).normalize();
        if (!alvo.startsWith(pasta)) {
            throw new ArquivoForaDaPastaException("caminho sai da pasta de notas");
        }
        if (!Files.exists(alvo, LinkOption.NOFOLLOW_LINKS)) {
            throw new ArquivoForaDaPastaException("arquivo inexistente");
        }
        if (!alvo.toRealPath().startsWith(pasta)) {
            throw new ArquivoForaDaPastaException("link simbólico aponta para fora da pasta");
        }
        return Files.readAllBytes(alvo);
    }

    /** Ex.: NF-2026-000123.pdf. Letras, dígitos, hífen e um único ".pdf" no fim; sem regex. */
    static boolean nomeValido(String arquivo) {
        if (arquivo == null || arquivo.length() < 5 || arquivo.length() > 60 || !arquivo.endsWith(".pdf")) {
            return false;
        }
        String base = arquivo.substring(0, arquivo.length() - 4);
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            boolean permitido = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-';
            if (!permitido) {
                return false;
            }
        }
        return true;
    }
}
