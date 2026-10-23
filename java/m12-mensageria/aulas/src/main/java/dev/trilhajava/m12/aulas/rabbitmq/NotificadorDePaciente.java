package dev.trilhajava.m12.aulas.rabbitmq;

/**
 * Avisa o paciente (SMS ou e-mail) de que o resultado está disponível.
 * Lança PacienteSemContatoException (permanente) ou NotificadorIndisponivelException (transitória).
 */
public interface NotificadorDePaciente {

    void avisar(ExameConcluido exame);
}
