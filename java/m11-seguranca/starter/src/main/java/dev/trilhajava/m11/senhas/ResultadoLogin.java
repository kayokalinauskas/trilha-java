package dev.trilhajava.m11.senhas;

public enum ResultadoLogin {
    SUCESSO,
    /** Usado tanto para "e-mail não existe" quanto para "senha errada": quem ataca não descobre quais e-mails existem. */
    CREDENCIAIS_INVALIDAS,
    BLOQUEADO
}
