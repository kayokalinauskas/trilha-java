package dev.trilhajava.m13.aulas.hexagonal.adaptador.web;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.NaoEncontradoException;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteiraAlteradaException;
import dev.trilhajava.m13.aulas.hexagonal.dominio.RegraDeFidelidadeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz as exceções do núcleo para HTTP (Problem Details, RFC 9457). O núcleo não sabe o que é um 422. */
@RestControllerAdvice(assignableTypes = ResgateController.class)
class ErrosDoResgate {

    @ExceptionHandler(RegraDeFidelidadeException.class)
    ProblemDetail regraViolada(RegraDeFidelidadeException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, erro.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ProblemDetail naoEncontrado(NaoEncontradoException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler(CarteiraAlteradaException.class)
    ProblemDetail conflito(CarteiraAlteradaException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, erro.getMessage());
    }
}
