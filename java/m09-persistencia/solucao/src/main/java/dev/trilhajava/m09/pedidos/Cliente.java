package dev.trilhajava.m09.pedidos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Cliente {

    @Id
    private Long id;

    private String nome;

    protected Cliente() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
