package dev.trilhajava.m11.aulas.autorizacao;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO de SAÍDA. Duas fábricas: com e sem anotações. O serviço escolhe qual usar;
 * não existe um "DTO completo" para alguém devolver por engano. Campo nulo some do JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProntuarioResposta(long id, String pacienteId, String medicoId, String queixa, String anotacoes) {

    public static ProntuarioResposta completo(Prontuario p) {
        return new ProntuarioResposta(p.id(), p.pacienteId(), p.medicoId(), p.queixa(), p.anotacoes());
    }

    public static ProntuarioResposta resumo(Prontuario p) {
        return new ProntuarioResposta(p.id(), p.pacienteId(), p.medicoId(), p.queixa(), null);
    }
}
