package dev.trilhajava.m11.aulas.autorizacao;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Problem Details (RFC 9457). O 404 tem um corpo fixo: nada nele depende de o prontuário existir. */
@RestControllerAdvice(basePackageClasses = TratadorDeErros.class)
public class TratadorDeErros {

    private static final String PROBLEMAS = "https://api.clinicas.dev/problemas/";

    @ExceptionHandler
    ProblemDetail naoEncontrado(ProntuarioNaoEncontradoException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Prontuário não encontrado");
        problema.setType(URI.create(PROBLEMAS + "nao-encontrado"));
        return problema;
    }

    @ExceptionHandler
    ProblemDetail negada(OperacaoNegadaException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problema.setType(URI.create(PROBLEMAS + "operacao-negada"));
        return problema;
    }

    @ExceptionHandler
    ProblemDetail medicoInvalido(MedicoInvalidoException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problema.setType(URI.create(PROBLEMAS + "medico-invalido"));
        return problema;
    }
}
