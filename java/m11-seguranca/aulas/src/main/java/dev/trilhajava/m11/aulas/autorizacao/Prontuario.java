package dev.trilhajava.m11.aulas.autorizacao;

/**
 * Um prontuário: pertence a UMA clínica (o tenant), a UM paciente e tem UM médico responsável.
 * As anotações clínicas são o dado mais sensível (dado de saúde, categoria especial na LGPD).
 */
public record Prontuario(
        long id,
        String clinicaId,
        String pacienteId,
        String medicoId,
        String queixa,
        String anotacoes) {

    public Prontuario comAnotacoes(String novasAnotacoes) {
        return new Prontuario(id, clinicaId, pacienteId, medicoId, queixa, novasAnotacoes);
    }
}
