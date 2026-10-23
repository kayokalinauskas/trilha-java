package dev.trilhajava.fintech.aplicacao;

/** Uma única mensagem para e-mail inexistente e senha errada: não revela quais e-mails têm conta. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
