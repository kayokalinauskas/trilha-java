package dev.trilhajava.m12.notificacoes;

/**
 * A integração com o provedor de e-mail (SES, SendGrid...). Pode falhar de forma transitória:
 * nesse caso lança {@link ProvedorIndisponivelException}.
 */
public interface EnviadorDeEmail {

    void enviar(String destinatario, String mensagem);
}
