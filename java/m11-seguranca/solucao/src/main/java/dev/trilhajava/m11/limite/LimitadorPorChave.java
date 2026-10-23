package dev.trilhajava.m11.limite;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Um balde por chave. {@code computeIfAbsent} cria o balde uma única vez, mesmo com duas requisições
 * da mesma chave chegando juntas.
 *
 * <p>Limitação consciente: o mapa cresce com cada IP novo. Em produção, use um cache com expiração
 * (Caffeine), uma biblioteca (Bucket4j) ou um armazenamento compartilhado entre instâncias (Redis).
 */
public class LimitadorPorChave {

    private final int capacidade;
    private final Duration periodo;
    private final Clock relogio;
    private final ConcurrentMap<String, BaldeDeFichas> baldes = new ConcurrentHashMap<>();

    public LimitadorPorChave(int capacidade, Duration periodo, Clock relogio) {
        this.capacidade = capacidade;
        this.periodo = periodo;
        this.relogio = relogio;
    }

    public Decisao tentar(String chave) {
        BaldeDeFichas balde = baldes.computeIfAbsent(chave, k -> new BaldeDeFichas(capacidade, periodo, relogio));
        synchronized (balde) {          // consumir + ler o que sobrou numa operação só
            boolean permitida = balde.tentarConsumir();
            return new Decisao(permitida, balde.fichasDisponiveis(), balde.tempoAteProximaFicha());
        }
    }
}
