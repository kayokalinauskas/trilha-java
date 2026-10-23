package dev.trilhajava.m09.transferencias;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

/**
 * Por que uma classe separada? A retentativa precisa de uma transação NOVA a cada tentativa.
 * Se o laço estivesse dentro de um método @Transactional, todas as tentativas usariam a mesma
 * transação (já condenada). E chamar this.transferir(...) de dentro do próprio serviço não passaria
 * pelo proxy do Spring — o @Transactional seria ignorado (o problema da auto-invocação).
 */
@Service
public class TransferenciasComRetentativa {

    static final int MAXIMO_DE_TENTATIVAS = 10;

    private final ServicoDeTransferencias servico;

    public TransferenciasComRetentativa(ServicoDeTransferencias servico) {
        this.servico = servico;
    }

    public void transferir(long origem, long destino, BigDecimal valor) {
        OptimisticLockingFailureException ultima = null;
        for (int tentativa = 1; tentativa <= MAXIMO_DE_TENTATIVAS; tentativa++) {
            try {
                servico.transferir(origem, destino, valor);
                return;
            } catch (OptimisticLockingFailureException conflito) {
                ultima = conflito;
                esperar(tentativa);
            }
        }
        throw new TransferenciaConcorrenteException(MAXIMO_DE_TENTATIVAS, ultima);
    }

    /** Backoff com jitter: espera um pouco mais a cada tentativa, com aleatoriedade para não "sincronizar" as threads. */
    private static void esperar(int tentativa) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(1, 5L * tentativa + 2));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
