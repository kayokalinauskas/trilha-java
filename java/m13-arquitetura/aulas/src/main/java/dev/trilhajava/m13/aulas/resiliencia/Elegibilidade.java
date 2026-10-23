package dev.trilhajava.m13.aulas.resiliencia;

/**
 * O que a recepção da clínica recebe ao conferir o convênio de um paciente.
 * NAO_CONFIRMADA é o fallback honesto: diz que não deu para saber, em vez de inventar "ativa".
 */
public record Elegibilidade(String carteirinha, Situacao situacao, String plano, String motivo) {

    public enum Situacao { ATIVA, INATIVA, NAO_CONFIRMADA }

    static Elegibilidade daOperadora(RespostaDaOperadora resposta) {
        Situacao situacao = resposta.ativa() ? Situacao.ATIVA : Situacao.INATIVA;
        return new Elegibilidade(resposta.carteirinha(), situacao, resposta.plano(), null);
    }

    static Elegibilidade naoConfirmada(String carteirinha, String motivo) {
        return new Elegibilidade(carteirinha, Situacao.NAO_CONFIRMADA, null, motivo);
    }
}
