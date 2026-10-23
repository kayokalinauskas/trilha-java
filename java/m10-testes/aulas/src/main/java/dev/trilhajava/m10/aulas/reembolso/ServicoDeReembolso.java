package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Reembolsa um pedido que o cliente desistiu de manter.
 *
 * <ol>
 *   <li>Pedido inexistente: {@link PedidoNaoEncontradoException}, e nada mais acontece.</li>
 *   <li>Só até 7 dias depois da entrega (direito de arrependimento do CDC, art. 49):
 *       depois disso, {@link ForaDoPrazoException}.</li>
 *   <li>Estorna o total do pedido no gateway, usando a autorização da cobrança original.</li>
 *   <li>Se o gateway falhar, a exceção sobe: nada volta ao estoque, nada é salvo, ninguém é avisado.</li>
 *   <li>Devolve cada item ao estoque, com a quantidade comprada.</li>
 *   <li>Salva o {@link Reembolso}, com o instante do relógio.</li>
 *   <li>Só depois de salvar, avisa o cliente. Devolve o reembolso salvo.</li>
 * </ol>
 */
public class ServicoDeReembolso {

    static final int PRAZO_EM_DIAS = 7;

    private final RepositorioDePedidos pedidos;
    private final GatewayDePagamento gateway;
    private final Estoque estoque;
    private final RepositorioDeReembolsos reembolsos;
    private final Notificador notificador;
    private final Clock clock;

    public ServicoDeReembolso(RepositorioDePedidos pedidos, GatewayDePagamento gateway, Estoque estoque,
            RepositorioDeReembolsos reembolsos, Notificador notificador, Clock clock) {
        this.pedidos = pedidos;
        this.gateway = gateway;
        this.estoque = estoque;
        this.reembolsos = reembolsos;
        this.notificador = notificador;
        this.clock = clock;
    }

    public Reembolso reembolsar(String numeroPedido) {
        Pedido pedido = pedidos.buscar(numeroPedido)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + numeroPedido));
        exigirDentroDoPrazo(pedido);

        BigDecimal valor = pedido.total();
        String codigoEstorno = gateway.estornar(pedido.autorizacaoPagamento(), valor);

        for (Item item : pedido.itens()) {
            estoque.devolver(item.sku(), item.quantidade());
        }

        Reembolso reembolso = new Reembolso(pedido.numero(), valor, codigoEstorno, Instant.now(clock));
        reembolsos.salvar(reembolso);
        notificador.reembolsoConcluido(pedido.cliente(), pedido.numero(), valor);
        return reembolso;
    }

    private void exigirDentroDoPrazo(Pedido pedido) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate ultimoDia = pedido.entregueEm().plusDays(PRAZO_EM_DIAS);
        if (hoje.isAfter(ultimoDia)) {
            throw new ForaDoPrazoException(
                    "Pedido " + pedido.numero() + " entregue em " + pedido.entregueEm()
                            + ": o prazo de arrependimento acabou em " + ultimoDia);
        }
    }
}
