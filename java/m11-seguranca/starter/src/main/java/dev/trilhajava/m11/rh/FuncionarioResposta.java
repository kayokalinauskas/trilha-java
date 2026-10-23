package dev.trilhajava.m11.rh;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

/** O que a API devolve. {@code salario} nulo some do JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FuncionarioResposta(long id, String nome, String departamento, Long gestorId, BigDecimal salario) {

    public static FuncionarioResposta comSalario(Funcionario f) {
        return new FuncionarioResposta(f.id(), f.nome(), f.departamento(), f.gestorId(), f.salario());
    }

    public static FuncionarioResposta semSalario(Funcionario f) {
        return new FuncionarioResposta(f.id(), f.nome(), f.departamento(), f.gestorId(), null);
    }
}
