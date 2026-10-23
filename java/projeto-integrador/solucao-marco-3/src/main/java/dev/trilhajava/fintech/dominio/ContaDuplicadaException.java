package dev.trilhajava.fintech.dominio;

public class ContaDuplicadaException extends RuntimeException {

    public ContaDuplicadaException(TipoConta tipo) {
        super("Já existe uma conta " + tipo + " para este CPF");
    }
}
