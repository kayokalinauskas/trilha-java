package dev.trilhajava.m12.aulas.retry;

/** A fronteira com o provedor de e-mail (em produção, a API de um fornecedor). */
public interface ProvedorDeEmail {

    void enviar(NotaEmitida nota);
}
