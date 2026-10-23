package dev.trilhajava.m13.loja.faturamento.internal;

import dev.trilhajava.m13.loja.faturamento.Fatura;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RegistroDeFaturas {

    private final Map<UUID, Fatura> faturas = new ConcurrentHashMap<>();

    public void salvar(Fatura fatura) {
        faturas.put(fatura.pedido(), fatura);
    }

    public Optional<Fatura> doPedido(UUID pedido) {
        return Optional.ofNullable(faturas.get(pedido));
    }
}
