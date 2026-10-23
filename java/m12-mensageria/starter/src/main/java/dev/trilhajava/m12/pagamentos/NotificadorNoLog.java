package dev.trilhajava.m12.pagamentos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Implementação de desenvolvimento. Os testes a substituem por um mock. */
@Component
class NotificadorNoLog implements NotificadorDePagamento {

    private static final Logger log = LoggerFactory.getLogger(NotificadorNoLog.class);

    @Override
    public void pagamentoAprovado(String pedidoId) {
        log.info("pagamento aprovado: pedido {}", pedidoId);
    }

    @Override
    public void pagamentoEstornado(String pedidoId) {
        log.info("pagamento estornado: pedido {}", pedidoId);
    }
}
