package dev.trilhajava.m13.aulas.hexagonal.dominio;

public class SaldoInsuficienteException extends RegraDeFidelidadeException {

    public SaldoInsuficienteException(String clienteId, Pontos saldo, Pontos custo) {
        super("cliente " + clienteId + " tem " + saldo.quantidade()
                + " pontos e a recompensa custa " + custo.quantidade());
    }
}
