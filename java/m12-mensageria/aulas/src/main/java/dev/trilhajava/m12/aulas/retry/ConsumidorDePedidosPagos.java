package dev.trilhajava.m12.aulas.retry;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Emite a nota fiscal de cada pedido pago. Não trata exceções: quem decide entre repetir e mandar
 * para o DLT é o DefaultErrorHandler. Um catch aqui "engoliria" a mensagem, e o offset avançaria.
 */
@Component
class ConsumidorDePedidosPagos {

    private final EmissorDeNotas emissor;
    private final JsonMapper json;

    ConsumidorDePedidosPagos(EmissorDeNotas emissor, JsonMapper json) {
        this.emissor = emissor;
        this.json = json;
    }

    /** Recebe String: o JSON é lido aqui, e um JSON quebrado vira uma JacksonException comum (não trava a partição). */
    @KafkaListener(topics = ConfiguracaoDeRetry.PEDIDOS_PAGOS, groupId = "emissao-de-notas")
    void receber(String mensagem) {
        PedidoPago pedido = json.readValue(mensagem, PedidoPago.class);
        if (pedido.numeroPedido() == null || pedido.valor() == null || pedido.valor().signum() <= 0) {
            throw new MensagemInvalidaException("pedido sem número ou com valor não positivo: " + mensagem);
        }
        emissor.emitir(pedido);
    }
}
