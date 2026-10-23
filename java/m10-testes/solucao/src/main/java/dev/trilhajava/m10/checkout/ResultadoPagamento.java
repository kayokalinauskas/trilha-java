package dev.trilhajava.m10.checkout;

public record ResultadoPagamento(boolean aprovado, String codigoAutorizacao, String motivoRecusa) {

    public static ResultadoPagamento aprovado(String codigoAutorizacao) {
        return new ResultadoPagamento(true, codigoAutorizacao, null);
    }

    public static ResultadoPagamento recusado(String motivo) {
        return new ResultadoPagamento(false, null, motivo);
    }
}
