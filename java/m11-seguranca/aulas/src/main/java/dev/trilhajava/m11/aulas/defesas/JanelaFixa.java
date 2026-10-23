package dev.trilhajava.m11.aulas.defesas;

import java.time.Clock;
import java.time.Duration;

/**
 * O algoritmo mais simples: um contador por janela de calendário (00:00–00:59, 01:00–01:59...),
 * zerado quando a janela vira. Existe aqui só para comparação com o token bucket.
 */
public class JanelaFixa {

    private final int limite;
    private final long janelaEmMillis;
    private final Clock relogio;

    private long janelaAtual = Long.MIN_VALUE;
    private int contador;

    public JanelaFixa(int limite, Duration janela, Clock relogio) {
        this.limite = limite;
        this.janelaEmMillis = janela.toMillis();
        this.relogio = relogio;
    }

    public synchronized boolean tentar() {
        long janela = Math.floorDiv(relogio.millis(), janelaEmMillis);
        if (janela != janelaAtual) {
            janelaAtual = janela;
            contador = 0;
        }
        if (contador >= limite) {
            return false;
        }
        contador++;
        return true;
    }
}
