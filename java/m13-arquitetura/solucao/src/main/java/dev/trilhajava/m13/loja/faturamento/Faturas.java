package dev.trilhajava.m13.loja.faturamento;

import dev.trilhajava.m13.loja.faturamento.internal.RegistroDeFaturas;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** A API pública do módulo faturamento: consultas. */
@Service
public class Faturas {

    private final RegistroDeFaturas registro;

    Faturas(RegistroDeFaturas registro) {
        this.registro = registro;
    }

    public Optional<Fatura> doPedido(UUID pedido) {
        return registro.doPedido(pedido);
    }
}
