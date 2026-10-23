package dev.trilhajava.m10.aulas.contrato.pedidos;

/** O serviço de frete não entrega no CEP informado (422 do provedor). */
public class FreteIndisponivelException extends RuntimeException {

    private final String cep;

    public FreteIndisponivelException(String cep) {
        super("Frete indisponível para o CEP " + cep);
        this.cep = cep;
    }

    public String cep() {
        return cep;
    }
}
