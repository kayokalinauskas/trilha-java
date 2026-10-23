package dev.trilhajava.m13.aulas.hexagonal.dominio;

/** Uma regra do programa de fidelidade foi violada. As subclasses dizem qual. */
public abstract class RegraDeFidelidadeException extends RuntimeException {

    protected RegraDeFidelidadeException(String mensagem) {
        super(mensagem);
    }
}
