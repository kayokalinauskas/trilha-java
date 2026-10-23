package dev.trilhajava.m13.loja.faturamento.internal;

import dev.trilhajava.m13.loja.faturamento.AutorizadorFiscal;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implementação de desenvolvimento. Os testes a substituem por um mock. */
@Component
class AutorizadorFiscalSimulado implements AutorizadorFiscal {

    @Override
    public String autorizar(UUID pedido, BigDecimal total) {
        return "3526" + pedido.toString().replace("-", "").substring(0, 20);
    }
}
