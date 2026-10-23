package dev.trilhajava.m15.cache;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * O "banco de dados": a fonte da verdade, lenta (cada consulta leva 50 ms) e contada, para os testes
 * saberem quantas vezes o cache deixou passar. Já vem pronto; não altere.
 */
@Component
public class BancoDeProdutos {

    static final long LATENCIA_MS = 50;

    private final Map<String, Produto> produtos = new ConcurrentHashMap<>();
    private final AtomicInteger consultas = new AtomicInteger();

    public BancoDeProdutos() {
        for (int i = 1; i <= 100; i++) {
            String id = "p" + i;
            produtos.put(id, new Produto(id, "Produto " + i, new BigDecimal(i + ".90")));
        }
    }

    public Optional<Produto> buscar(String id) {
        consultas.incrementAndGet();
        try {
            Thread.sleep(LATENCIA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return Optional.ofNullable(produtos.get(id));
    }

    public void salvar(Produto produto) {
        produtos.put(produto.id(), produto);
    }

    public int consultas() {
        return consultas.get();
    }

    public void zerarContagem() {
        consultas.set(0);
    }
}
