package dev.trilhajava.m08.pacientes;

import java.time.LocalDate;

/**
 * O que a API devolve. Separar o DTO de resposta do modelo interno permite mudar um sem
 * quebrar o outro — e decidir o que expor: aqui o CPF sai mascarado (LGPD: minimização de dados).
 */
public record PacienteResposta(long id, String nome, String cpf, String email, LocalDate nascimento) {

    static PacienteResposta de(Paciente p) {
        return new PacienteResposta(p.id(), p.nome(), mascarar(p.cpf()), p.email(), p.nascimento());
    }

    /** 52998224725 → ***.982.247-** (o padrão usado pelo governo federal em documentos públicos). */
    static String mascarar(String cpf) {
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}
