package dev.trilhajava.m11.aulas.owasp.erros;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * VULNERÁVEL DE PROPÓSITO (A10, CWE-209). Não copie.
 * "Para facilitar o suporte", devolve ao cliente a mensagem, a classe e o começo do stack trace.
 */
@RestControllerAdvice
public class TratadorQueVaza {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> qualquerErro(Exception e) {
        var corpo = new LinkedHashMap<String, Object>();
        corpo.put("erro", e.getMessage());
        corpo.put("tipo", e.getClass().getName());
        corpo.put("pilha", Arrays.stream(e.getStackTrace()).limit(3).map(StackTraceElement::toString).toList());
        return corpo;
    }
}
