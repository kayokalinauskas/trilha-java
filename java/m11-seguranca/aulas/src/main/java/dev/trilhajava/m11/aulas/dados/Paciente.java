package dev.trilhajava.m11.aulas.dados;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Um paciente da clínica. O CPF fica em duas colunas: cifrado (para ler de volta) e como blind
 * index (para buscar). Nenhuma das duas guarda o número em claro.
 */
@Entity
@Table(name = "paciente", indexes = @Index(name = "ux_paciente_cpf_indice", columnList = "cpf_indice", unique = true))
public class Paciente {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Convert(converter = ConversorDeCpfCifrado.class)
    @Column(name = "cpf_cifrado", nullable = false, length = 120)
    private Cpf cpf;

    @Column(name = "cpf_indice", nullable = false, length = 64)
    private String cpfIndice;

    protected Paciente() {
        // exigido pelo JPA
    }

    public Paciente(String nome, Cpf cpf, String cpfIndice) {
        this.nome = nome;
        this.cpf = cpf;
        this.cpfIndice = cpfIndice;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public String getCpfIndice() {
        return cpfIndice;
    }
}
