package dev.trilhajava.m11.aulas.owasp.erros;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Tratador de último recurso: o cliente recebe um Problem Details (RFC 9457) genérico com um código de
 * rastreio; o log recebe a exceção inteira com o MESMO código. O suporte cruza os dois.
 */
@RestControllerAdvice
public class TratadorSeguro {

    private static final Logger log = LoggerFactory.getLogger(TratadorSeguro.class);

    /** Erro do cliente: dá para explicar sem revelar nada interno. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + e.getName() + "' está em formato inválido.");
        problema.setType(URI.create("https://api.fintech.exemplo/erros/parametro-invalido"));
        return problema;
    }

    /** Qualquer outra coisa: mensagem genérica para fora, detalhes só no log. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        String codigo = UUID.randomUUID().toString();
        log.error("erro-inesperado codigo={}", codigo, e);
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível concluir a operação. Informe o código ao suporte.");
        problema.setType(URI.create("https://api.fintech.exemplo/erros/interno"));
        problema.setProperty("codigoDeRastreio", codigo);
        return problema;
    }
}
