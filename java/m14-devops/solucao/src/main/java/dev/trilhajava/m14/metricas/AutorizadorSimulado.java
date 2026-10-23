package dev.trilhajava.m14.metricas;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Implementação de desenvolvimento: recusa acima de R$ 10.000. */
@Component
class AutorizadorSimulado implements Autorizador {

    @Override
    public boolean autorizar(Pagamento pagamento) {
        return pagamento.valor().compareTo(new BigDecimal("10000")) <= 0;
    }
}
