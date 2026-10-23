package dev.trilhajava.m09.pedidos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Produto {

    @Id
    private Long id;

    private String nome;

    private BigDecimal preco;

    protected Produto() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
