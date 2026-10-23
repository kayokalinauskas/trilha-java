package dev.trilhajava.m10.aulas.contrato.frete;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A API do serviço de frete: o lado PROVEDOR do contrato. */
@RestController
@RequestMapping("/fretes")
public class CotacaoController {

    private final CalculadoraDeFrete calculadora;

    public CotacaoController(CalculadoraDeFrete calculadora) {
        this.calculadora = calculadora;
    }

    @PostMapping("/cotacoes")
    public Cotacao cotar(@Valid @RequestBody PedidoDeCotacao pedido) {
        return calculadora.cotar(pedido);
    }

    @ExceptionHandler(CepSemCoberturaException.class)
    ProblemDetail semCobertura(CepSemCoberturaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problema.setType(URI.create("https://frete.dev/problemas/cep-sem-cobertura"));
        problema.setTitle("CEP sem cobertura");
        problema.setProperty("cep", e.cep());
        return problema;
    }
}
