package dev.trilhajava.m14.logs;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Toda resposta de erro leva o requestId: é o que o cliente manda para o suporte, e o que o
 * suporte procura nos logs.
 */
@RestControllerAdvice
class TratadorDeErros {

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(PedidoNaoEncontradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Pedido não encontrado");
        problema.setProperty("requestId", MDC.get(FiltroDeCorrelacao.CHAVE_MDC));
        return problema;
    }
}
