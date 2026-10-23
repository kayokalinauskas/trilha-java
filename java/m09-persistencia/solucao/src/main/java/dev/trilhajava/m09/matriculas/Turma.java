package dev.trilhajava.m09.matriculas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String disciplina;

    @Column(nullable = false)
    private int vagas;

    protected Turma() {
    }

    public Turma(String codigo, String disciplina, int vagas) {
        this.codigo = codigo;
        this.disciplina = disciplina;
        this.vagas = vagas;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public int getVagas() {
        return vagas;
    }
}
