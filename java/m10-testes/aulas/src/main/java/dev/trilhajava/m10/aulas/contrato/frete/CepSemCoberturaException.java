package dev.trilhajava.m10.aulas.contrato.frete;

public class CepSemCoberturaException extends RuntimeException {

    private final String cep;

    public CepSemCoberturaException(String cep) {
        super("Não entregamos no CEP " + cep);
        this.cep = cep;
    }

    public String cep() {
        return cep;
    }
}
