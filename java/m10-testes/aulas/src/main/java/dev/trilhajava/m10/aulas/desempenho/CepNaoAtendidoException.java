package dev.trilhajava.m10.aulas.desempenho;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CepNaoAtendidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CepNaoAtendidoException(String cep) {
        super("CEP sem cobertura de entrega: " + cep);
    }
}
