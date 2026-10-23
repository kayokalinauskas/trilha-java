package dev.trilhajava.m12.aulas.fundamentos;

/** O serviço vizinho não respondeu (fora do ar, timeout, recusa de conexão). */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
