package dev.trilhajava.m13.loja.estoque.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Detalhe interno do módulo estoque. */
@Component
public class Saldos {

    private final Map<String, Integer> saldos = new ConcurrentHashMap<>(Map.of(
            "CAMISETA-P", 10,
            "CAMISETA-M", 10,
            "CANECA", 50));

    public int de(String sku) {
        return saldos.getOrDefault(sku, 0);
    }

    public void baixar(String sku, int quantidade) {
        saldos.merge(sku, -quantidade, Integer::sum);
    }
}
