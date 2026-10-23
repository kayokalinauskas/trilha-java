package dev.trilhajava.m11.aulas.owasp.logs;

import java.util.function.Consumer;

/**
 * Registra tentativas de login num log de texto, uma linha por evento.
 * O destino é um Consumer para o teste capturar as linhas; em produção, seria um Logger do SLF4J.
 */
public class AuditoriaDeLogin {

    private final Consumer<String> log;
    private final boolean neutralizar;

    private AuditoriaDeLogin(Consumer<String> log, boolean neutralizar) {
        this.log = log;
        this.neutralizar = neutralizar;
    }

    /** VULNERÁVEL DE PROPÓSITO: grava o e-mail exatamente como chegou. */
    public static AuditoriaDeLogin vulneravel(Consumer<String> log) {
        return new AuditoriaDeLogin(log, false);
    }

    public static AuditoriaDeLogin segura(Consumer<String> log) {
        return new AuditoriaDeLogin(log, true);
    }

    public void falhou(String email, String ip) {
        log.accept("WARN login-falhou email=" + tratar(email) + " ip=" + tratar(ip));
    }

    private String tratar(String valor) {
        return neutralizar ? NeutralizadorDeLog.limpar(valor) : valor;
    }
}
