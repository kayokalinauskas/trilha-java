package dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.cliente;

/** Exemplo analisado pelo ArchUnit (violação: camadas). */
public class Cliente {

    private final String nome;

    public Cliente(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
