package dev.trilhajava.m11.aulas.senhas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueio progressivo por e-mail: as 3 primeiras falhas seguidas são livres; a partir da 4ª,
 * a próxima tentativa só é aceita depois de uma espera que dobra a cada falha (30 s, 1 min,
 * 2 min...), até o teto de 15 minutos. Com 100 falhas seguidas, a senha deixa de valer e só a
 * recuperação de senha libera a conta (o limite da NIST SP 800-63B rev. 4).
 *
 * <p>A chave é o e-mail digitado (normalizado), exista a conta ou não: assim a resposta para
 * um e-mail inexistente se comporta igual à de um existente.
 */
public class ControleDeTentativas {

    static final int FALHAS_LIVRES = 3;
    static final Duration PRIMEIRA_ESPERA = Duration.ofSeconds(30);
    static final Duration ESPERA_MAXIMA = Duration.ofMinutes(15);
    static final int LIMITE_ABSOLUTO = 100;

    private record Estado(int falhas, Instant liberadoEm) {
    }

    private final Map<String, Estado> porEmail = new ConcurrentHashMap<>();
    private final Clock clock;

    public ControleDeTentativas(Clock clock) {
        this.clock = clock;
    }

    /** Vazio se pode tentar agora; senão, o instante a partir do qual pode. */
    public Optional<Instant> esperaAte(String email) {
        Estado estado = porEmail.get(email);
        if (estado == null || estado.liberadoEm() == null) {
            return Optional.empty();
        }
        return clock.instant().isBefore(estado.liberadoEm())
                ? Optional.of(estado.liberadoEm())
                : Optional.empty();
    }

    public boolean exigeRecuperacao(String email) {
        Estado estado = porEmail.get(email);
        return estado != null && estado.falhas() >= LIMITE_ABSOLUTO;
    }

    public void registrarFalha(String email) {
        // compute() é atômico por chave no ConcurrentHashMap: duas falhas simultâneas contam duas.
        porEmail.compute(email, (chave, anterior) -> {
            int falhas = anterior == null ? 1 : anterior.falhas() + 1;
            return new Estado(falhas, falhas > FALHAS_LIVRES ? clock.instant().plus(espera(falhas)) : null);
        });
    }

    public void limpar(String email) {
        porEmail.remove(email);
    }

    /** 4ª falha: 30 s; 5ª: 1 min; 6ª: 2 min; ... nunca mais que 15 min. */
    static Duration espera(int falhas) {
        int dobras = Math.min(falhas - FALHAS_LIVRES - 1, 10);
        Duration espera = PRIMEIRA_ESPERA.multipliedBy(1L << dobras);
        return espera.compareTo(ESPERA_MAXIMA) > 0 ? ESPERA_MAXIMA : espera;
    }
}
