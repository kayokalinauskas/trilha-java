package dev.trilhajava.m10.arquitetura.exemplos.violacao.ciclo.dominio.cliente;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.ciclo.dominio.conta.Conta;
import java.util.ArrayList;
import java.util.List;

/** Exemplo analisado pelo ArchUnit (violação: ciclo). */
public class Cliente {

    private final String nome;

    private final List<Conta> contas = new ArrayList<>();

    public List<Conta> contas() {
        return contas;
    }

    public Cliente(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
