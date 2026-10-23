package dev.trilhajava.m10.arquitetura.exemplos.limpo.dominio.cliente;

/** Exemplo analisado pelo ArchUnit (código limpo). */
public class Cliente {

    private final String nome;

    public Cliente(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
