package dev.trilhajava.m11.aulas.owasp.arquivos;

/** Nome de arquivo recusado. A API responde 404, sem dizer se o arquivo existe em outro lugar. */
public class ArquivoForaDaPastaException extends RuntimeException {

    public ArquivoForaDaPastaException(String motivo) {
        super(motivo);
    }
}
