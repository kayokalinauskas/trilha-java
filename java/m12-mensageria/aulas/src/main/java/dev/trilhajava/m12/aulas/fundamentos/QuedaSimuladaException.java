package dev.trilhajava.m12.aulas.fundamentos;

/** Simula o processo do consumidor morrendo (deploy, falta de memória, kill -9) no pior momento. */
public class QuedaSimuladaException extends RuntimeException {

    public QuedaSimuladaException(String mensagem) {
        super(mensagem);
    }
}
