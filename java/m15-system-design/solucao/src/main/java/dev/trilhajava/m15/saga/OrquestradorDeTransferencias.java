package dev.trilhajava.m15.saga;

import dev.trilhajava.m15.saga.BancoParceiro.Resposta;
import dev.trilhajava.m15.saga.Transferencia.Estado;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga orquestrada de uma transferência para outro banco. Não existe transação que abranja os dois bancos,
 * então cada passo é local e tem a sua compensação:
 *
 * <pre>
 *   1. debitar a origem aqui            (compensação: estornar)
 *   2. enviar ao banco parceiro         ACEITA   → CONCLUIDA
 *                                       RECUSADA → estornar → COMPENSADA
 *                                       sem resposta → PENDENTE (NÃO estorna)
 * </pre>
 *
 * Timeout não é falha: o pedido pode ter chegado ao parceiro. Estornar às cegas deixaria o cliente com o
 * dinheiro aqui E lá. Uma transferência PENDENTE só se resolve consultando o parceiro ({@link #reconciliar}).
 * Todos os passos usam ids derivados do id da transferência, então repetir qualquer um é seguro.
 */
public class OrquestradorDeTransferencias {

    private final Contas contas;
    private final BancoParceiro parceiro;
    private final Map<String, Transferencia> transferencias = new ConcurrentHashMap<>();

    public OrquestradorDeTransferencias(Contas contas, BancoParceiro parceiro) {
        this.contas = contas;
        this.parceiro = parceiro;
    }

    public Optional<Transferencia> buscar(String id) {
        return Optional.ofNullable(transferencias.get(id));
    }

    public synchronized Transferencia iniciar(String id, String origem, String contaDestino, BigDecimal valor) {
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("valor deve ser positivo");
        }
        Transferencia existente = transferencias.get(id);
        if (existente != null) {
            return existente;   // idempotente: o cliente repetiu o pedido
        }
        var transferencia = new Transferencia(id, origem, contaDestino, valor, Estado.PENDENTE);
        if (!contas.debitar(origem, valor, "debito-" + id)) {
            return salvar(transferencia.com(Estado.RECUSADA));
        }
        salvar(transferencia);   // débito feito: a partir daqui, a saga precisa terminar (ou ser reconciliada)
        try {
            Resposta resposta = parceiro.enviar(id, contaDestino, valor);
            return concluirCom(transferencia, resposta);
        } catch (ParceiroIndisponivelException e) {
            return transferencia;   // PENDENTE
        }
    }

    /** Resolve uma transferência PENDENTE perguntando ao parceiro. Nos outros estados, não faz nada. */
    public synchronized Transferencia reconciliar(String id) {
        Transferencia transferencia = buscar(id)
                .orElseThrow(() -> new IllegalArgumentException("Transferência desconhecida: " + id));
        if (transferencia.estado() != Estado.PENDENTE) {
            return transferencia;
        }
        try {
            // Vazio = o parceiro nunca recebeu: é seguro desfazer. (Reenviar também seria uma opção.)
            Resposta resposta = parceiro.consultar(id).orElse(Resposta.RECUSADA);
            return concluirCom(transferencia, resposta);
        } catch (ParceiroIndisponivelException e) {
            return transferencia;   // continua PENDENTE; tentamos de novo mais tarde
        }
    }

    private Transferencia concluirCom(Transferencia transferencia, Resposta resposta) {
        if (resposta == Resposta.ACEITA) {
            return salvar(transferencia.com(Estado.CONCLUIDA));
        }
        contas.creditar(transferencia.origem(), transferencia.valor(), "estorno-" + transferencia.id());
        return salvar(transferencia.com(Estado.COMPENSADA));
    }

    private Transferencia salvar(Transferencia transferencia) {
        transferencias.put(transferencia.id(), transferencia);
        return transferencia;
    }
}
