package dev.trilhajava.fintech.dominio;

import java.util.UUID;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(UUID id) {
        super("Conta " + id + " não encontrada");
    }
}
