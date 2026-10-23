package dev.trilhajava.m11.aulas.autorizacao;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * TODAS as regras de acesso a prontuários, num lugar só. Listagem, busca, edição e exames usam estas funções.
 * O nome do bean ("regras") é o que as expressões SpEL usam: @regras.podeVerExames(...).
 */
@Component("regras")
public class RegrasDeProntuario {

    private final RepositorioDeProntuarios repositorio;

    public RegrasDeProntuario(RepositorioDeProntuarios repositorio) {
        this.repositorio = repositorio;
    }

    /** Ver o prontuário (ao menos o resumo). A clínica vem antes de tudo: outro tenant nunca vê. */
    public boolean podeVer(Solicitante quem, Prontuario prontuario) {
        if (!prontuario.clinicaId().equals(quem.clinicaId())) {
            return false;
        }
        return switch (quem.papel()) {
            case PACIENTE -> prontuario.pacienteId().equals(quem.usuario());
            case MEDICO -> prontuario.medicoId().equals(quem.usuario());
            case ATENDENTE -> true;
        };
    }

    /** Ver as anotações clínicas: só o paciente e o médico responsável. A recepção vê o resumo. */
    public boolean veAnotacoes(Solicitante quem, Prontuario prontuario) {
        return podeVer(quem, prontuario) && !quem.eh(Papel.ATENDENTE);
    }

    /** Escrever anotações: só o médico responsável. */
    public boolean podeEditarAnotacoes(Solicitante quem, Prontuario prontuario) {
        return podeVer(quem, prontuario) && quem.eh(Papel.MEDICO);
    }

    /** Exames seguem a regra das anotações. Versão para SpEL: recebe o Authentication e o id. */
    public boolean podeVerExames(Authentication autenticacao, long prontuarioId) {
        Solicitante quem = Solicitante.de(autenticacao);
        return repositorio.porIdNaClinica(prontuarioId, quem.clinicaId())
                .filter(p -> veAnotacoes(quem, p))
                .isPresent();
    }
}
