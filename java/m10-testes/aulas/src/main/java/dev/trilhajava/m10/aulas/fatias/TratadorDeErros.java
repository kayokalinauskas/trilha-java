package dev.trilhajava.m10.aulas.fatias;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Todos os erros da API saem no formato ProblemDetail (RFC 9457), com Content-Type application/problem+json. */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    ProblemDetail produtoNaoEncontrado(ProdutoNaoEncontradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setType(URI.create("https://loja.dev/problemas/produto-nao-encontrado"));
        problema.setTitle("Produto não encontrado");
        return problema;
    }

    @ExceptionHandler
    ProblemDetail estoqueInsuficiente(EstoqueInsuficienteException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problema.setType(URI.create("https://loja.dev/problemas/estoque-insuficiente"));
        problema.setTitle("Estoque insuficiente");
        problema.setProperty("solicitado", e.getSolicitado());
        problema.setProperty("disponivel", e.getDisponivel());
        return problema;
    }

    /** Erros de @Valid: o 400 padrão do Spring, mais um mapa campo → mensagem. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erros = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = ex.getBody();
        problema.setType(URI.create("https://loja.dev/problemas/dados-invalidos"));
        problema.setTitle("Dados inválidos");
        problema.setDetail("O corpo da requisição tem " + erros.size() + " campo(s) inválido(s)");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }
}
