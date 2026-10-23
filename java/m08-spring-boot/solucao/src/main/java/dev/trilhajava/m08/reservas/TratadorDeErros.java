package dev.trilhajava.m08.reservas;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz TODAS as falhas da API para o formato Problem Details (RFC 9457).
 *
 * Estender ResponseEntityExceptionHandler faz as exceções do próprio Spring MVC (405, 404 de rota,
 * 415, parâmetro com tipo errado...) também saírem como application/problem+json. Aqui só
 * personalizamos as que importam e acrescentamos as exceções de negócio.
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);
    private static final String BASE = "https://api.hotel.dev/problemas/";

    /** Um item da lista "erros" das falhas de validação. */
    record ErroDeCampo(String campo, String mensagem) {
    }

    @ExceptionHandler(ReservaNaoEncontradaException.class)
    public ProblemDetail reservaNaoEncontrada(ReservaNaoEncontradaException e) {
        return problema(HttpStatus.NOT_FOUND, "reserva-nao-encontrada", "Reserva não encontrada", e.getMessage());
    }

    @ExceptionHandler(QuartoIndisponivelException.class)
    public ProblemDetail quartoIndisponivel(QuartoIndisponivelException e) {
        ProblemDetail problema = problema(HttpStatus.CONFLICT, "quarto-indisponivel", "Quarto indisponível", e.getMessage());
        problema.setProperty("quartoId", e.quartoId()); // extensão: dado útil para o cliente reagir
        return problema;
    }

    @ExceptionHandler(PeriodoInvalidoException.class)
    public ProblemDetail periodoInvalido(PeriodoInvalidoException e) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "periodo-invalido", "Período inválido", e.getMessage());
    }

    /** A rede de segurança: nada vaza para o cliente, tudo vai para o log com um código de rastreio. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        String codigo = UUID.randomUUID().toString();
        log.error("Erro inesperado [codigoRastreio={}]", codigo, e);
        ProblemDetail problema = problema(HttpStatus.INTERNAL_SERVER_ERROR, "erro-interno", "Erro interno",
                "Ocorreu um erro inesperado. Informe o código de rastreio ao suporte.");
        problema.setProperty("codigoRastreio", codigo);
        return problema;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErroDeCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField).thenComparing(FieldError::getDefaultMessage))
                .map(erro -> new ErroDeCampo(erro.getField(), erro.getDefaultMessage()))
                .toList();
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos",
                "Um ou mais campos estão inválidos.");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // A mensagem original do Jackson expõe detalhes internos (classes, posições): não repasse.
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "requisicao-malformada", "Requisição malformada",
                "O corpo da requisição não é um JSON válido ou tem campos com tipo errado.");
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    private static ProblemDetail problema(HttpStatus status, String tipo, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create(BASE + tipo));
        problema.setTitle(titulo);
        return problema; // "instance" é preenchido pelo Spring com o caminho da requisição
    }
}
