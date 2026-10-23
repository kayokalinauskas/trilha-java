package dev.trilhajava.m11.aulas.autorizacao;

/** Vira 404. Serve tanto para "não existe" quanto para "existe, mas você não pode ver". */
public class ProntuarioNaoEncontradoException extends RuntimeException {

    public ProntuarioNaoEncontradoException(long id) {
        super("Prontuário " + id + " não encontrado");
    }
}
