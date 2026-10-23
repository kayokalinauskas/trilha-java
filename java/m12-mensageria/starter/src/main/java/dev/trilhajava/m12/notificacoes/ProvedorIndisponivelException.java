package dev.trilhajava.m12.notificacoes;

/** Falha transitória do provedor (timeout, 503): tentar de novo pode dar certo. */
public class ProvedorIndisponivelException extends RuntimeException {

    public ProvedorIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
