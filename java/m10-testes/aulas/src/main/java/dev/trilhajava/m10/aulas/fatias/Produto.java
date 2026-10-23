package dev.trilhajava.m10.aulas.fatias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class Produto {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sku;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int quantidadeEmEstoque;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Categoria categoria;

    protected Produto() {
        // exigido pelo JPA
    }

    public Produto(String sku, String nome, BigDecimal preco, int quantidadeEmEstoque, Categoria categoria) {
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = quantidadeEmEstoque;
        this.categoria = categoria;
    }

    /** Tira do estoque; recusa se não houver o suficiente. */
    public void baixar(int quantidade) {
        if (quantidade > quantidadeEmEstoque) {
            throw new EstoqueInsuficienteException(sku, quantidade, quantidadeEmEstoque);
        }
        quantidadeEmEstoque -= quantidade;
    }

    public void desativar() {
        ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
