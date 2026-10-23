package dev.trilhajava.m11.limite;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Token bucket com aritmética inteira em nanossegundos. Em vez de guardar "2,5 fichas" num double
 * (e acumular erro de arredondamento), guarda o TEMPO acumulado: cada ficha vale {@code nanosPorFicha}.
 *
 * <p>Nada de thread de reposição: as fichas são calculadas sob demanda, a partir do tempo que passou
 * desde a última consulta ("lazy refill"). {@code synchronized} garante que duas threads não consumam
 * a mesma ficha.
 */
public class BaldeDeFichas {

    private final long nanosPorFicha;
    private final long capacidadeEmNanos;
    private final Clock relogio;

    private long acumulado;          // fichas × nanosPorFicha
    private Instant ultimaReposicao;

    public BaldeDeFichas(int capacidade, Duration periodo, Clock relogio) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("capacidade deve ser positiva");
        }
        if (periodo.isNegative() || periodo.isZero()) {
            throw new IllegalArgumentException("período deve ser positivo");
        }
        this.nanosPorFicha = periodo.toNanos() / capacidade;
        this.capacidadeEmNanos = nanosPorFicha * capacidade;
        this.relogio = relogio;
        this.acumulado = capacidadeEmNanos;             // começa cheio
        this.ultimaReposicao = relogio.instant();
    }

    public synchronized boolean tentarConsumir() {
        repor();
        if (acumulado < nanosPorFicha) {
            return false;
        }
        acumulado -= nanosPorFicha;
        return true;
    }

    public synchronized int fichasDisponiveis() {
        repor();
        return (int) (acumulado / nanosPorFicha);
    }

    public synchronized Duration tempoAteProximaFicha() {
        repor();
        return Duration.ofNanos(Math.max(0, nanosPorFicha - acumulado));
    }

    private void repor() {
        Instant agora = relogio.instant();
        long passou = Duration.between(ultimaReposicao, agora).toNanos();
        if (passou <= 0) {
            return;                     // relógio parado ou voltando (ajuste de NTP): não repõe nem "desrepõe"
        }
        acumulado = Math.min(capacidadeEmNanos, acumulado + passou);
        ultimaReposicao = agora;
    }
}
