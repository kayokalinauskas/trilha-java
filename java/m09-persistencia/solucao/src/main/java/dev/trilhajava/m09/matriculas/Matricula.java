package dev.trilhajava.m09.matriculas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

/**
 * A associação aluno ↔ turma com dados próprios. Um @ManyToMany "puro" não teria onde guardar
 * status e data — por isso a tabela de junção vira entidade, com dois @ManyToOne.
 */
@Entity
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: @ManyToOne é EAGER por padrão no JPA — quase nunca é o que você quer.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @Enumerated(EnumType.STRING) // ORDINAL quebraria os dados se alguém reordenasse o enum
    @Column(nullable = false, length = 10)
    private StatusMatricula status;

    @Column(name = "matriculada_em", nullable = false)
    private Instant matriculadaEm;

    protected Matricula() {
    }

    public Matricula(Aluno aluno, Turma turma, Instant quando) {
        this.aluno = aluno;
        this.turma = turma;
        this.status = StatusMatricula.ATIVA;
        this.matriculadaEm = quando;
    }

    public void reativar(Instant quando) {
        this.status = StatusMatricula.ATIVA;
        this.matriculadaEm = quando;
    }

    public void cancelar() {
        this.status = StatusMatricula.CANCELADA;
    }

    public boolean ativa() {
        return status == StatusMatricula.ATIVA;
    }

    public Long getId() {
        return id;
    }
}
