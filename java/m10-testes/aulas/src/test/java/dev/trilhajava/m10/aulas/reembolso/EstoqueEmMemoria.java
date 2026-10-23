package dev.trilhajava.m10.aulas.reembolso;

import java.util.HashMap;
import java.util.Map;

/** Fake: um estoque que funciona de verdade, só que num Map. */
class EstoqueEmMemoria implements Estoque {

    private final Map<String, Integer> saldos = new HashMap<>();

    EstoqueEmMemoria comSaldo(String sku, int quantidade) {
        saldos.put(sku, quantidade);
        return this;
    }

    @Override
    public void devolver(String sku, int quantidade) {
        saldos.merge(sku, quantidade, Integer::sum);
    }

    int saldo(String sku) {
        return saldos.getOrDefault(sku, 0);
    }
}
