package dev.trilhajava.m10.aulas.fatias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Categoria {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    protected Categoria() {
        // exigido pelo JPA
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
