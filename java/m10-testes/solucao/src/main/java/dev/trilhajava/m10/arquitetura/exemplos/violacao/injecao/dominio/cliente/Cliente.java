package dev.trilhajava.m10.arquitetura.exemplos.violacao.injecao.dominio.cliente;

/** Exemplo analisado pelo ArchUnit (violação: injecao). */
public class Cliente {

    private final String nome;

    public Cliente(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
