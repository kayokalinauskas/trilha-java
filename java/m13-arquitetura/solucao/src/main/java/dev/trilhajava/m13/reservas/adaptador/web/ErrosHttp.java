package dev.trilhajava.m13.reservas.adaptador.web;

import dev.trilhajava.m13.reservas.dominio.ConflitoDeHorarioException;
import dev.trilhajava.m13.reservas.dominio.NaoEncontradoException;
import dev.trilhajava.m13.reservas.dominio.OperacaoNaoPermitidaException;
import dev.trilhajava.m13.reservas.dominio.ReservaInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** A tradução das exceções do núcleo para HTTP também é trabalho do adaptador web. */
@RestControllerAdvice
class ErrosHttp {

    @ExceptionHandler(NaoEncontradoException.class)
    ProblemDetail naoEncontrado(NaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", e);
    }

    @ExceptionHandler(ConflitoDeHorarioException.class)
    ProblemDetail conflito(ConflitoDeHorarioException e) {
        return problema(HttpStatus.CONFLICT, "Conflito de horário", e);
    }

    @ExceptionHandler(ReservaInvalidaException.class)
    ProblemDetail invalida(ReservaInvalidaException e) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Reserva inválida", e);
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    ProblemDetail naoPermitida(OperacaoNaoPermitidaException e) {
        return problema(HttpStatus.FORBIDDEN, "Operação não permitida", e);
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, RuntimeException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        p.setTitle(titulo);
        return p;
    }
}
