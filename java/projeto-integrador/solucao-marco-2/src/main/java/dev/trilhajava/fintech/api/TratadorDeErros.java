package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.ContaDuplicadaException;
import dev.trilhajava.fintech.dominio.ContaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.ChaveIdempotenciaReutilizadaException;
import dev.trilhajava.fintech.dominio.SaldoInsuficienteException;
import dev.trilhajava.fintech.dominio.TransferenciaInvalidaException;
import dev.trilhajava.fintech.dominio.TransferenciaNaoEncontradaException;
import java.net.URI;
import java.util.ArrayList;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Todos os erros no formato Problem Details (RFC 9457), com type em https://fintech.dev/problemas/. */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);
    private static final String BASE = "https://fintech.dev/problemas/";

    record ErroDeCampo(String campo, String mensagem) {
    }

    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ProblemDetail naoEncontrada(ContaNaoEncontradaException e) {
        return problema(HttpStatus.NOT_FOUND, "conta-nao-encontrada", "Conta não encontrada", e.getMessage());
    }

    @ExceptionHandler(ContaDuplicadaException.class)
    public ProblemDetail duplicada(ContaDuplicadaException e) {
        return problema(HttpStatus.CONFLICT, "conta-duplicada", "Conta duplicada", e.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ProblemDetail saldoInsuficiente(SaldoInsuficienteException e) {
        ProblemDetail problema = problema(HttpStatus.UNPROCESSABLE_CONTENT, "saldo-insuficiente", "Saldo insuficiente",
                e.getMessage());
        problema.setProperty("saldoDisponivel", e.saldoDisponivel());
        return problema;
    }

    @ExceptionHandler(TransferenciaNaoEncontradaException.class)
    public ProblemDetail transferenciaNaoEncontrada(TransferenciaNaoEncontradaException e) {
        return problema(HttpStatus.NOT_FOUND, "transferencia-nao-encontrada", "Transferência não encontrada", e.getMessage());
    }

    @ExceptionHandler(TransferenciaInvalidaException.class)
    public ProblemDetail transferenciaInvalida(TransferenciaInvalidaException e) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "transferencia-invalida", "Transferência inválida", e.getMessage());
    }

    @ExceptionHandler(ChaveIdempotenciaReutilizadaException.class)
    public ProblemDetail chaveReutilizada(ChaveIdempotenciaReutilizadaException e) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "chave-idempotencia-reutilizada",
                "Chave de idempotência reutilizada", e.getMessage());
    }

    /** Uma constraint do banco barrou uma corrida que passou pelas verificações da aplicação. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflitoNoBanco(DataIntegrityViolationException e) {
        log.warn("Violação de integridade tratada como conflito: {}", e.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "requisicao-concorrente", "Requisição concorrente",
                "Outra requisição alterou os mesmos dados ao mesmo tempo. Tente novamente.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentoInvalido(IllegalArgumentException e) {
        return problema(HttpStatus.BAD_REQUEST, "requisicao-invalida", "Requisição inválida", e.getMessage());
    }

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
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "requisicao-malformada", "Requisição malformada",
                "O corpo da requisição não é um JSON válido ou tem campos com tipo errado.");
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    /**
     * Quando um método tem restrições em parâmetros além do @RequestBody (aqui, @NotBlank no header
     * Idempotency-Key), o Spring valida o método inteiro e lança HandlerMethodValidationException —
     * inclusive para os erros do corpo. Sem este handler, esses 400 sairiam sem o nosso formato.
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErroDeCampo> erros = new ArrayList<>();
        for (ParameterValidationResult resultado : ex.getParameterValidationResults()) {
            if (resultado instanceof ParameterErrors errosDoObjeto) {
                errosDoObjeto.getFieldErrors().forEach(e -> erros.add(new ErroDeCampo(e.getField(), e.getDefaultMessage())));
            } else {
                String parametro = resultado.getMethodParameter().getParameterName();
                resultado.getResolvableErrors().forEach(e -> erros.add(new ErroDeCampo(parametro, e.getDefaultMessage())));
            }
        }
        erros.sort(Comparator.comparing(ErroDeCampo::campo));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos",
                "Um ou mais campos estão inválidos.");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    private static ProblemDetail problema(HttpStatus status, String tipo, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create(BASE + tipo));
        problema.setTitle(titulo);
        return problema;
    }
}
