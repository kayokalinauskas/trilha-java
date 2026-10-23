package dev.trilhajava.m10.aulas.junit;

/** O horário pedido não pode ser agendado: fora do expediente, em cima da hora ou já ocupado. */
public class HorarioIndisponivelException extends RuntimeException {

    public HorarioIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
