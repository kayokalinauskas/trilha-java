package dev.trilhajava.m13.aulas.microsservicos.pedidos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Saga orquestrada: o serviço de pedidos conduz os passos e, se um deles falha,
 * desfaz os anteriores com compensações. Não existe transação entre serviços.
 *
 * <ol>
 *   <li>reservar o estoque (HTTP, no serviço de estoque);</li>
 *   <li>cobrar o pagamento (no serviço de pagamentos);</li>
 *   <li>se a cobrança for recusada, liberar a reserva (compensação).</li>
 * </ol>
 */
public class SagaDeCriacaoDePedido {

    public record NovoPedido(String pedidoId, String sku, int quantidade, BigDecimal valor) {}

    public enum Status { CONFIRMADO, REJEITADO, CANCELADO, COMPENSACAO_PENDENTE }

    public record Resultado(String pedidoId, Status status, List<String> passos) {}

    private static final Logger log = LoggerFactory.getLogger(SagaDeCriacaoDePedido.class);

    private final EstoqueCliente estoque;
    private final Pagamentos pagamentos;

    public SagaDeCriacaoDePedido(EstoqueCliente estoque, Pagamentos pagamentos) {
        this.estoque = estoque;
        this.pagamentos = pagamentos;
    }

    public Resultado executar(NovoPedido pedido) {
        List<String> passos = new ArrayList<>();
        String id = pedido.pedidoId();

        try {
            estoque.reservar(new EstoqueCliente.PedidoDeReserva(id, pedido.sku(), pedido.quantidade()));
            registrar(id, passos, "estoque reservado: " + pedido.quantidade() + " x " + pedido.sku());
        } catch (HttpClientErrorException.Conflict e) {
            registrar(id, passos, "estoque recusou a reserva (409)");
            return new Resultado(id, Status.REJEITADO, passos);   // nada a desfazer
        } catch (ResourceAccessException e) {
            // Timeout NÃO quer dizer "não reservou": quer dizer "não sei".
            registrar(id, passos, "sem resposta do estoque: " + e.getMessage());
            return compensar(id, passos);
        }

        Pagamentos.Resposta cobranca = pagamentos.cobrar(id, pedido.valor());
        if (cobranca == Pagamentos.Resposta.RECUSADO) {
            registrar(id, passos, "pagamento recusado");
            return compensar(id, passos);
        }
        registrar(id, passos, "pagamento aprovado: R$ " + pedido.valor());
        registrar(id, passos, "pedido confirmado");
        return new Resultado(id, Status.CONFIRMADO, passos);
    }

    private Resultado compensar(String id, List<String> passos) {
        try {
            estoque.liberar(id);                                   // idempotente do lado do estoque
            registrar(id, passos, "compensação: reserva liberada");
            return new Resultado(id, Status.CANCELADO, passos);
        } catch (RestClientException e) {
            // Em produção: o estado da saga fica salvo e um job tenta de novo depois.
            registrar(id, passos, "compensação falhou, fica pendente: " + e.getMessage());
            return new Resultado(id, Status.COMPENSACAO_PENDENTE, passos);
        }
    }

    private static void registrar(String id, List<String> passos, String passo) {
        passos.add(passo);
        log.info("[{}] {}", id, passo);
    }
}
