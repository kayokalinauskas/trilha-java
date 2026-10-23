package dev.trilhajava.m02.grafica;

/** Validação compartilhada pelas formas: falhar na criação, nunca no cálculo. */
final class Dimensoes {

    private Dimensoes() {
    }

    static double positiva(double valor, String nome) {
        if (!(valor > 0) || Double.isInfinite(valor)) { // !(x > 0) também rejeita NaN
            throw new IllegalArgumentException(nome + " deve ser maior que zero, recebido: " + valor);
        }
        return valor;
    }
}
