package dev.trilhajava.m11.aulas.autorizacao;

/** Vira 422. A mesma mensagem para "médico não existe" e "médico é de outra clínica". */
public class MedicoInvalidoException extends RuntimeException {

    public MedicoInvalidoException() {
        super("Médico inválido para esta clínica");
    }
}
