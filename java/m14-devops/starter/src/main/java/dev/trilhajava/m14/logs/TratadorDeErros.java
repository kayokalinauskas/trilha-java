package dev.trilhajava.m14.logs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class TratadorDeErros {

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(PedidoNaoEncontradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Pedido não encontrado");
        // TODO: inclua a propriedade "requestId" (o id de correlação desta requisição).
        return problema;
    }
}
