package dev.trilhajava.m12.aulas.retry;

/** Falha TRANSITÓRIA: o serviço do outro lado está fora agora, mas pode voltar. Vale tentar de novo, esperando. */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
