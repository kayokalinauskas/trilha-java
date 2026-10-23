package dev.trilhajava.m15.saga;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * As contas do nosso banco (em memória). Débito e crédito são IDEMPOTENTES pelo id da operação: repetir
 * uma operação já aplicada não muda o saldo. É isso que permite ao orquestrador repetir passos sem medo.
 * Já vem pronta; não altere.
 */
public class Contas {

    private final Map<String, BigDecimal> saldos = new HashMap<>();
    private final Set<String> aplicadas = new HashSet<>();

    public synchronized void abrir(String conta, String saldoInicial) {
        saldos.put(conta, new BigDecimal(saldoInicial));
    }

    public synchronized BigDecimal saldo(String conta) {
        return saldos.getOrDefault(conta, BigDecimal.ZERO);
    }

    /** @return false se não houver saldo (e nada muda); true se debitou agora ou se já tinha debitado antes */
    public synchronized boolean debitar(String conta, BigDecimal valor, String idOperacao) {
        if (aplicadas.contains(idOperacao)) {
            return true;
        }
        BigDecimal saldo = saldo(conta);
        if (saldo.compareTo(valor) < 0) {
            return false;
        }
        saldos.put(conta, saldo.subtract(valor));
        aplicadas.add(idOperacao);
        return true;
    }

    public synchronized void creditar(String conta, BigDecimal valor, String idOperacao) {
        if (aplicadas.add(idOperacao)) {
            saldos.put(conta, saldo(conta).add(valor));
        }
    }
}
