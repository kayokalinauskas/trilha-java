package dev.trilhajava.m13.aulas.hexagonal.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A carteira de pontos de um cliente: a entidade que guarda as regras do resgate.
 * Java puro: nenhuma anotação de framework, nenhum conhecimento de banco ou de HTTP.
 */
public class CarteiraDePontos {

    private final String clienteId;
    private Pontos saldo;
    private final long versao;   // controle de concorrência otimista; o domínio só a carrega

    private CarteiraDePontos(String clienteId, Pontos saldo, long versao) {
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.saldo = Objects.requireNonNull(saldo, "saldo");
        this.versao = versao;
    }

    /** Recria uma carteira que já existe (quem chama é o adaptador de persistência). */
    public static CarteiraDePontos reconstituir(String clienteId, Pontos saldo, long versao) {
        return new CarteiraDePontos(clienteId, saldo, versao);
    }

    public Resgate resgatar(Recompensa recompensa, Instant agora) {
        if (!recompensa.ativa()) {
            throw new RecompensaIndisponivelException(recompensa.codigo());
        }
        if (saldo.menorQue(recompensa.custo())) {
            throw new SaldoInsuficienteException(clienteId, saldo, recompensa.custo());
        }
        saldo = saldo.menos(recompensa.custo());
        return new Resgate(UUID.randomUUID().toString(), clienteId, recompensa.codigo(),
                recompensa.custo(), saldo, agora);
    }

    public String clienteId() {
        return clienteId;
    }

    public Pontos saldo() {
        return saldo;
    }

    public long versao() {
        return versao;
    }
}
