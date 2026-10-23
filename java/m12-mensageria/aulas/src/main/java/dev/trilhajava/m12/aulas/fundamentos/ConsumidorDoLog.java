package dev.trilhajava.m12.aulas.fundamentos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lê o log como um grupo de consumidores e entrega cada evento a um destino.
 * A {@link Garantia} decide a ordem entre confirmar e processar; {@code idempotente}
 * liga o descarte de duplicatas pelo id do evento.
 */
public final class ConsumidorDoLog {

    private final LogDeEventos<PedidoPago> log;
    private final String grupo;
    private final ServicoDestino destino;
    private final Garantia garantia;
    private final boolean idempotente;
    // Em memória só para a aula. Em produção: uma tabela, gravada na MESMA transação do efeito (aula 5).
    private final Set<UUID> jaProcessados = new HashSet<>();
    private boolean cairNoMeioDoProximo;

    public ConsumidorDoLog(LogDeEventos<PedidoPago> log, String grupo, ServicoDestino destino,
                           Garantia garantia, boolean idempotente) {
        this.log = log;
        this.grupo = grupo;
        this.destino = destino;
        this.garantia = garantia;
        this.idempotente = idempotente;
    }

    /** Arma uma queda entre os dois passos (confirmar e processar) do próximo evento. */
    public void cairNoMeioDoProximo() {
        cairNoMeioDoProximo = true;
    }

    /**
     * Processa tudo o que está pendente para o grupo e devolve quantos eventos tratou.
     * Se o destino estiver fora do ar, a exceção sobe e o que não foi confirmado fica no log.
     */
    public int processarPendentes() {
        int tratados = 0;
        for (LogDeEventos.Registro<PedidoPago> registro : log.ler(grupo, Integer.MAX_VALUE)) {
            long proximo = registro.offset() + 1;
            if (garantia == Garantia.NO_MAXIMO_UMA_VEZ) {
                log.confirmar(grupo, proximo);
                quedaSeArmada(registro);
                executar(registro.valor());
            } else {
                executar(registro.valor());
                quedaSeArmada(registro);
                log.confirmar(grupo, proximo);
            }
            tratados++;
        }
        return tratados;
    }

    private void executar(PedidoPago pago) {
        if (idempotente && jaProcessados.contains(pago.idDoEvento())) {
            return; // duplicata: o efeito já aconteceu; basta seguir e confirmar
        }
        destino.processar(pago);
        if (idempotente) {
            jaProcessados.add(pago.idDoEvento());
        }
    }

    private void quedaSeArmada(LogDeEventos.Registro<PedidoPago> registro) {
        if (cairNoMeioDoProximo) {
            cairNoMeioDoProximo = false;
            throw new QuedaSimuladaException("consumidor " + grupo + " caiu no offset " + registro.offset());
        }
    }
}
