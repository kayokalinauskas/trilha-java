package dev.trilhajava.m11.aulas.autorizacao;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Os casos de uso. Cada método recebe o Solicitante explícito e aplica, nesta ordem:
 * 1) papel (função) com @PreAuthorize; 2) tenant e objeto na CONSULTA; 3) operação sobre o objeto visível.
 */
@Service
public class ServicoDeProntuarios {

    private final RepositorioDeProntuarios repositorio;
    private final RegrasDeProntuario regras;
    private final DiretorioDeUsuarios diretorio;
    private final RegistroDeNegacoes auditoria;

    public ServicoDeProntuarios(RepositorioDeProntuarios repositorio, RegrasDeProntuario regras,
            DiretorioDeUsuarios diretorio, RegistroDeNegacoes auditoria) {
        this.repositorio = repositorio;
        this.regras = regras;
        this.diretorio = diretorio;
        this.auditoria = auditoria;
    }

    /** Listagem: o filtro vai na consulta, nunca "carregar tudo e filtrar depois". */
    public List<ProntuarioResposta> listar(Solicitante quem) {
        List<Prontuario> visiveis = switch (quem.papel()) {
            case PACIENTE -> repositorio.doPaciente(quem.clinicaId(), quem.usuario());
            case MEDICO -> repositorio.doMedico(quem.clinicaId(), quem.usuario());
            case ATENDENTE -> repositorio.daClinica(quem.clinicaId());
        };
        return visiveis.stream().map(p -> resposta(quem, p)).toList();
    }

    public ProntuarioResposta buscar(Solicitante quem, long id) {
        return resposta(quem, visivel(quem, id, "ler"));
    }

    @PreAuthorize("hasRole('ATENDENTE')")
    public ProntuarioResposta criar(Solicitante quem, NovoProntuario novo) {
        if (!diretorio.ehDaClinica(novo.medicoId(), quem.clinicaId(), Papel.MEDICO)
                || !diretorio.ehDaClinica(novo.pacienteId(), quem.clinicaId(), Papel.PACIENTE)) {
            throw new MedicoInvalidoException();
        }
        var prontuario = new Prontuario(repositorio.proximoId(), quem.clinicaId(), // a clínica vem do TOKEN
                novo.pacienteId(), novo.medicoId(), novo.queixa(), null);
        return ProntuarioResposta.resumo(repositorio.salvar(prontuario));
    }

    @PreAuthorize("hasRole('MEDICO')")
    public ProntuarioResposta atualizarAnotacoes(Solicitante quem, long id, NovasAnotacoes novas) {
        Prontuario prontuario = visivel(quem, id, "editar-anotacoes"); // 1º: pode ver? senão 404
        if (!regras.podeEditarAnotacoes(quem, prontuario)) {             // 2º: pode fazer? senão 403
            auditoria.registrar(quem, "editar-anotacoes", "prontuario/" + id, "403");
            throw new OperacaoNegadaException("Só o médico responsável altera as anotações");
        }
        return ProntuarioResposta.completo(repositorio.salvar(prontuario.comAnotacoes(novas.anotacoes())));
    }

    /** Consulta escopada pelo tenant + regra do objeto. "Não existe" e "não é seu" saem iguais. */
    private Prontuario visivel(Solicitante quem, long id, String operacao) {
        return repositorio.porIdNaClinica(id, quem.clinicaId())
                .filter(p -> regras.podeVer(quem, p))
                .orElseThrow(() -> {
                    auditoria.registrar(quem, operacao, "prontuario/" + id, "404");
                    return new ProntuarioNaoEncontradoException(id);
                });
    }

    private ProntuarioResposta resposta(Solicitante quem, Prontuario p) {
        return regras.veAnotacoes(quem, p) ? ProntuarioResposta.completo(p) : ProntuarioResposta.resumo(p);
    }
}
