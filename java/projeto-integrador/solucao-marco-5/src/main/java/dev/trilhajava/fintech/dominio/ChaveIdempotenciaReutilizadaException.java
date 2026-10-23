package dev.trilhajava.fintech.dominio;

public class ChaveIdempotenciaReutilizadaException extends RuntimeException {

    public ChaveIdempotenciaReutilizadaException(String chave) {
        super("A chave de idempotência " + chave + " já foi usada com outro conteúdo");
    }
}
