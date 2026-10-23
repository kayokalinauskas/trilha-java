package dev.trilhajava.m09.matriculas;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeMatriculas {

    private final AlunoRepository alunos;
    private final TurmaRepository turmas;
    private final MatriculaRepository matriculas;
    private final Clock clock = Clock.systemUTC();

    public ServicoDeMatriculas(AlunoRepository alunos, TurmaRepository turmas, MatriculaRepository matriculas) {
        this.alunos = alunos;
        this.turmas = turmas;
        this.matriculas = matriculas;
    }

    /** Matricula (ou reativa uma matrícula cancelada) e devolve o id da matrícula. */
    @Transactional
    public long matricular(long alunoId, long turmaId) {
        Aluno aluno = alunos.findById(alunoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Aluno " + alunoId));
        // O lock na turma serializa as matrículas da MESMA turma (e só dela).
        Turma turma = turmas.buscarParaAtualizar(turmaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Turma " + turmaId));

        var existente = matriculas.findByAlunoIdAndTurmaId(alunoId, turmaId);
        if (existente.isPresent() && existente.get().ativa()) {
            throw new MatriculaDuplicadaException(alunoId, turmaId);
        }
        if (matriculas.countByTurmaIdAndStatus(turmaId, StatusMatricula.ATIVA) >= turma.getVagas()) {
            throw new TurmaLotadaException(turma.getCodigo());
        }
        if (existente.isPresent()) {
            existente.get().reativar(clock.instant()); // dirty checking: o UPDATE sai no commit
            return existente.get().getId();
        }
        return matriculas.save(new Matricula(aluno, turma, clock.instant())).getId();
    }

    @Transactional
    public void cancelar(long alunoId, long turmaId) {
        Matricula matricula = matriculas.findByAlunoIdAndTurmaId(alunoId, turmaId)
                .filter(Matricula::ativa)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Matrícula ativa"));
        matricula.cancelar();
    }

    @Transactional(readOnly = true)
    public List<String> turmasDoAluno(long alunoId) {
        return matriculas.codigosDasTurmasAtivas(alunoId);
    }

    @Transactional(readOnly = true)
    public List<AlunoMatriculado> listaDeChamada(String codigoTurma) {
        return matriculas.listaDeChamada(codigoTurma);
    }

    @Transactional(readOnly = true)
    public long vagasRestantes(String codigoTurma) {
        Turma turma = turmas.findByCodigo(codigoTurma)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Turma " + codigoTurma));
        return turma.getVagas() - matriculas.countByTurmaIdAndStatus(turma.getId(), StatusMatricula.ATIVA);
    }
}
