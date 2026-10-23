package dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida;

/** Parte do contrato da porta: a carteira mudou entre a leitura e a gravação (conflito otimista). */
public class CarteiraAlteradaException extends RuntimeException {

    public CarteiraAlteradaException(String clienteId) {
        super("a carteira do cliente " + clienteId + " foi alterada por outra operação; tente de novo");
    }
}
