package dev.trilhajava.m08.pacientes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 409 Conflict: a requisição é válida, mas conflita com o estado atual do recurso. */
@ResponseStatus(HttpStatus.CONFLICT)
public class CpfJaCadastradoException extends RuntimeException {

    public CpfJaCadastradoException() {
        super("Já existe um paciente com este CPF");
    }
}
