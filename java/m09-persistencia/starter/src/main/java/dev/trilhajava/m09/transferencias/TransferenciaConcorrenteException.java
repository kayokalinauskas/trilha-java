package dev.trilhajava.m09.transferencias;

/** As retentativas acabaram: a conta estava sendo alterada por outras transações o tempo todo. */
public class TransferenciaConcorrenteException extends RuntimeException {

    public TransferenciaConcorrenteException(int tentativas, Throwable causa) {
        super("Transferência não concluída após " + tentativas + " tentativas por concorrência", causa);
    }
}
