package dev.trilhajava.m11.aulas.defesas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token bucket por chave (usuário, IP, API key), guardando UM instante por chave: o momento em que o
 * balde daquela chave estará cheio de novo ("cheio em"). É a forma do GCRA (Generic Cell Rate
 * Algorithm), equivalente ao token bucket e fácil de levar para o Redis, porque o estado é um só valor.
 *
 * <ul>
 *   <li>Sem entrada no mapa, ou com "cheio em" no passado: o balde está cheio.</li>
 *   <li>Gastar {@code custo} créditos empurra "cheio em" para frente em {@code custo × intervalo}.</li>
 *   <li>Se, depois de empurrar, faltar mais que {@code tempoParaEncher} para encher, o balde não
 *       tinha créditos suficientes: recusa, sem mudar nada.</li>
 * </ul>
 *
 * <p>Nada de thread de reposição: o tempo vem só do {@link Clock}, e a "reposição" é a passagem do tempo.
 */
public class LimitadorDeCreditos {

    private final PoliticaDeLimite politica;
    private final Clock relogio;
    private final Map<String, Instant> cheioEm = new ConcurrentHashMap<>();

    public LimitadorDeCreditos(PoliticaDeLimite politica, Clock relogio) {
        this.politica = politica;
        this.relogio = relogio;
    }

    public PoliticaDeLimite politica() {
        return politica;
    }

    public Consumo consumir(String chave, int custo) {
        if (custo <= 0 || custo > politica.capacidade()) {
            throw new IllegalArgumentException(
                    "custo deve estar entre 1 e " + politica.capacidade() + ": " + custo);
        }
        Instant agora = relogio.instant();
        Duration custoEmTempo = politica.intervaloPorCredito().multipliedBy(custo);
        Duration limite = politica.tempoParaEncher();
        Consumo[] resultado = new Consumo[1];

        // compute() é atômico POR CHAVE: duas requisições da mesma chave não leem o mesmo estado.
        cheioEm.compute(chave, (k, anterior) -> {
            Instant base = (anterior == null || anterior.isBefore(agora)) ? agora : anterior;
            Instant depois = base.plus(custoEmTempo);
            Duration faltariaParaEncher = Duration.between(agora, depois);
            if (faltariaParaEncher.compareTo(limite) > 0) {
                resultado[0] = new Consumo(false, restantes(base, agora),
                        faltariaParaEncher.minus(limite), Duration.between(agora, base));
                return anterior;                               // recusa não gasta nada
            }
            resultado[0] = new Consumo(true, restantes(depois, agora), Duration.ZERO, faltariaParaEncher);
            return depois;
        });
        return resultado[0];
    }

    /** Créditos disponíveis agora, sem gastar nada. */
    public int disponiveis(String chave) {
        Instant agora = relogio.instant();
        Instant estado = cheioEm.get(chave);
        return estado == null ? politica.capacidade() : restantes(estado, agora);
    }

    /**
     * Remove as chaves cujo balde já encheu: para o algoritmo, "cheio" e "ausente" são a mesma coisa.
     * Chame periodicamente (um @Scheduled, por exemplo) para o mapa não crescer para sempre.
     */
    public int removerOciosos() {
        Instant agora = relogio.instant();
        int removidas = 0;
        for (Map.Entry<String, Instant> entrada : cheioEm.entrySet()) {
            // remove(chave, valor) é condicional: se outra thread acabou de gastar, o valor mudou e nada sai.
            if (!entrada.getValue().isAfter(agora) && cheioEm.remove(entrada.getKey(), entrada.getValue())) {
                removidas++;
            }
        }
        return removidas;
    }

    public int chavesEmMemoria() {
        return cheioEm.size();
    }

    private int restantes(Instant estado, Instant agora) {
        long ocupado = Math.max(0, Duration.between(agora, estado).toNanos());
        long livre = politica.tempoParaEncher().toNanos() - ocupado;
        return (int) Math.max(0, livre / politica.intervaloPorCredito().toNanos());
    }
}
