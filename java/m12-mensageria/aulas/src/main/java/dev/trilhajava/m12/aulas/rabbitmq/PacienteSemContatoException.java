package dev.trilhajava.m12.aulas.rabbitmq;

/** Falha permanente: o cadastro do paciente não tem telefone nem e-mail. */
public class PacienteSemContatoException extends RuntimeException {

    public PacienteSemContatoException(String pacienteId) {
        super("Paciente sem contato cadastrado: " + pacienteId);
    }
}
