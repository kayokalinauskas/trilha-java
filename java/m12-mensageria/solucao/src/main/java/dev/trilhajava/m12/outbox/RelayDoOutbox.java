package dev.trilhajava.m12.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lê a tabela de outbox e publica os eventos pendentes no broker. Garantia: pelo menos uma vez
 * (at-least-once) e, dentro de um agregado, na ordem de criação.
 *
 * Numa aplicação Spring, {@link #executar()} roda num @Scheduled a cada poucos segundos.
 */
public class RelayDoOutbox {

    private final RepositorioDeSaida repositorio;
    private final Publicador publicador;
    private final Clock relogio;
    private final ConfiguracaoDoRelay config;

    public RelayDoOutbox(RepositorioDeSaida repositorio, Publicador publicador, Clock relogio, ConfiguracaoDoRelay config) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
        this.config = config;
    }

    public Resultado executar() {
        Instant agora = relogio.instant();
        // Um agregado "travado" não publica mais nada nesta execução: publicar o evento seguinte
        // passaria à frente do que ficou para trás e quebraria a ordem.
        Set<String> travados = new HashSet<>(repositorio.agregadosComEventoMorto());
        int publicados = 0;
        int falhas = 0;
        int adiados = 0;

        for (EventoDeSaida evento : repositorio.pendentes(config.tamanhoDoLote())) {
            if (travados.contains(evento.agregado()) || evento.proximaTentativa().isAfter(agora)) {
                travados.add(evento.agregado());
                adiados++;
                continue;
            }
            try {
                publicador.publicar(evento);
                repositorio.salvar(evento.publicado(agora));
                publicados++;
            } catch (RuntimeException erro) {
                // Qualquer exceção: o relay não pode morrer por causa de um evento.
                repositorio.salvar(falhou(evento, erro, agora));
                travados.add(evento.agregado());
                falhas++;
            }
        }
        return new Resultado(publicados, falhas, adiados);
    }

    private EventoDeSaida falhou(EventoDeSaida evento, RuntimeException erro, Instant agora) {
        String mensagem = erro.getClass().getSimpleName() + ": " + erro.getMessage();
        int tentativas = evento.tentativas() + 1;
        if (tentativas >= config.maxTentativas()) {
            return evento.morto(mensagem);
        }
        return evento.falhou(mensagem, agora.plus(espera(tentativas)));
    }

    /** 1ª falha: atrasoInicial; depois dobra a cada falha, até o teto. */
    Duration espera(int tentativas) {
        Duration espera = config.atrasoInicial();
        for (int i = 1; i < tentativas && espera.compareTo(config.atrasoMaximo()) < 0; i++) {
            espera = espera.multipliedBy(2);
        }
        return espera.compareTo(config.atrasoMaximo()) > 0 ? config.atrasoMaximo() : espera;
    }

    /** Devolve um evento MORTO à fila (depois que alguém corrigiu a causa). */
    public void reprocessar(UUID id) {
        EventoDeSaida evento = repositorio.buscar(id)
                .orElseThrow(() -> new IllegalArgumentException("evento não encontrado: " + id));
        if (evento.status() != EventoDeSaida.Status.MORTO) {
            throw new IllegalStateException("só eventos MORTOS podem ser reprocessados; este está " + evento.status());
        }
        repositorio.salvar(evento.reativado(relogio.instant()));
    }

    /** Apaga os eventos publicados há mais tempo que a retenção. Pendentes e mortos nunca são apagados. */
    public int limpar(Duration retencao) {
        Instant limite = relogio.instant().minus(retencao);
        int removidos = 0;
        for (EventoDeSaida evento : repositorio.todos()) {
            if (evento.status() == EventoDeSaida.Status.PUBLICADO && evento.publicadoEm().isBefore(limite)) {
                repositorio.remover(evento.id());
                removidos++;
            }
        }
        return removidos;
    }
}
