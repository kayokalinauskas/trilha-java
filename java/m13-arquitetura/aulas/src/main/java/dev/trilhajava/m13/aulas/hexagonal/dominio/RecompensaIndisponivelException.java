package dev.trilhajava.m13.aulas.hexagonal.dominio;

public class RecompensaIndisponivelException extends RegraDeFidelidadeException {

    public RecompensaIndisponivelException(String codigo) {
        super("a recompensa " + codigo + " não está disponível para resgate");
    }
}
