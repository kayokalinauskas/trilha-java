package dev.trilhajava.m08.pacientes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** @ResponseStatus: o Spring traduz a exceção em 404. (No exercício 08.03 você padroniza o corpo desses erros.) */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PacienteNaoEncontradoException extends RuntimeException {

    public PacienteNaoEncontradoException(long id) {
        super("Paciente " + id + " não encontrado");
    }
}
