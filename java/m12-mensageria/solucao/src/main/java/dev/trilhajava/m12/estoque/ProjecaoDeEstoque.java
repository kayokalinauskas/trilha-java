package dev.trilhajava.m12.estoque;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * O "banco" da projeção: o saldo atual de cada SKU, montado a partir dos movimentos.
 * Um saldo nunca pode ficar negativo: se ficar, o movimento é inválido (ou chegou fora de ordem).
 */
@Component
public class ProjecaoDeEstoque {

    private final Map<String, Integer> saldos = new ConcurrentHashMap<>();

    /**
     * @throws SaldoNegativoException se o movimento deixaria o saldo negativo (falha permanente)
     * @throws BancoIndisponivelException numa falha transitória de infraestrutura
     */
    public void aplicar(MovimentoDeEstoque movimento) {
        saldos.compute(movimento.sku(), (sku, atual) -> {
            int novo = (atual == null ? 0 : atual) + movimento.quantidade();
            if (novo < 0) {
                throw new SaldoNegativoException(sku, atual == null ? 0 : atual, movimento.quantidade());
            }
            return novo;
        });
    }

    public int saldo(String sku) {
        return saldos.getOrDefault(sku, 0);
    }
}
