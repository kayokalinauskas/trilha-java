package dev.trilhajava.m11.aulas.senhas;

/** A saída de e-mails do portal. Em produção, um provedor SMTP; nos testes, uma lista. */
public interface CaixaDeEmail {

    void enviar(String para, String assunto, String corpo);
}
