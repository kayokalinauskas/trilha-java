package dev.trilhajava.m16.ferramentas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pronta: as contas e as propostas de Pix, em memória. Faz de conta que é o serviço de contas do banco.
 * Repare que {@link #confirmar} não é ferramenta do assistente: quem confirma é o cliente, pelo app.
 */
public class Contas {

    private final Map<String, BigDecimal> saldos = new HashMap<>();
    private final List<PropostaDePix> propostas = new ArrayList<>();

    public void abrir(String cliente, BigDecimal saldoInicial) {
        saldos.put(cliente, saldoInicial);
    }

    public Optional<BigDecimal> saldo(String cliente) {
        return Optional.ofNullable(saldos.get(cliente));
    }

    public PropostaDePix registrarProposta(String cliente, String chave, BigDecimal valor) {
        var proposta = new PropostaDePix("pix-" + (propostas.size() + 1), cliente, chave, valor, PropostaDePix.Status.PENDENTE);
        propostas.add(proposta);
        return proposta;
    }

    public List<PropostaDePix> propostas() {
        return List.copyOf(propostas);
    }

    /** Chamado pelo app quando o cliente toca em "confirmar" (com senha ou biometria). */
    public PropostaDePix confirmar(String idProposta, String cliente) {
        for (int i = 0; i < propostas.size(); i++) {
            var p = propostas.get(i);
            if (p.id().equals(idProposta) && p.cliente().equals(cliente) && p.status() == PropostaDePix.Status.PENDENTE) {
                BigDecimal saldo = saldos.get(cliente);
                if (saldo.compareTo(p.valor()) < 0) {
                    throw new IllegalStateException("saldo insuficiente");
                }
                saldos.put(cliente, saldo.subtract(p.valor()));
                var confirmada = new PropostaDePix(p.id(), p.cliente(), p.chave(), p.valor(), PropostaDePix.Status.CONFIRMADA);
                propostas.set(i, confirmada);
                return confirmada;
            }
        }
        throw new IllegalArgumentException("proposta não encontrada: " + idProposta);
    }
}
