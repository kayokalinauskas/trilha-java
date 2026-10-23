package dev.trilhajava.m06;

import java.util.List;

/**
 * Exercício 06.01 — Contador de acessos usado por TODAS as threads do servidor ao mesmo tempo.
 * Rode: mvn test -Dtest=ContadorDeAcessosTest
 *
 * Proibido: synchronized em todos os métodos (funciona, mas serializa o servidor inteiro).
 */
public class ContadorDeAcessos {

    public void registrar(String endpoint) {
        throw new UnsupportedOperationException("TODO");
    }

    public long total(String endpoint) {
        throw new UnsupportedOperationException("TODO");
    }

    public long totalGeral() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<String> topEndpoints(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
