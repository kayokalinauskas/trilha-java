package dev.trilhajava.m11.aulas.defesas;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Herdar de ResponseEntityExceptionHandler transforma os erros do Spring MVC (header obrigatório ausente,
 * parâmetro fora dos limites, JSON inválido) em Problem Details (RFC 9457).
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    ProblemDetail chaveReutilizada(ChaveDeIdempotenciaReutilizadaException erro) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, erro.getMessage());
        problema.setTitle("Idempotency-Key reutilizada");
        return problema;
    }
}
