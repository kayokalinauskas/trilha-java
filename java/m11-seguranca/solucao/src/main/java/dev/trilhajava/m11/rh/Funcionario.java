package dev.trilhajava.m11.rh;

import java.math.BigDecimal;

/**
 * Um funcionário. {@code usuario} é o login (o mesmo nome de {@code Authentication.getName()});
 * {@code gestorId} é o id do gestor direto (ou {@code null}).
 */
public record Funcionario(long id, String usuario, String nome, String departamento, Long gestorId, BigDecimal salario) {

    public Funcionario comSalario(BigDecimal novo) {
        return new Funcionario(id, usuario, nome, departamento, gestorId, novo);
    }
}
