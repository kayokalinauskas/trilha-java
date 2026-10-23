package dev.trilhajava.m13.aulas.padroes.fornecedor;

/** A exceção do SDK quando o serviço do fornecedor não responde. */
public class PagaFacilIndisponivelException extends RuntimeException {

    private final int httpStatus;

    public PagaFacilIndisponivelException(int httpStatus, String mensagem) {
        super(mensagem);
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
