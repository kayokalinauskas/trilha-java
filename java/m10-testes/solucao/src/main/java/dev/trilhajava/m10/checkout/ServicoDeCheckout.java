package dev.trilhajava.m10.checkout;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Exercício 10.02 — o código a testar (pronto e correto). Os requisitos estão no enunciado, no site.
 *
 * <p>As regras do checkout:
 *
 * <ol>
 *   <li>Pedido sem itens, ou item com quantidade <= 0 → IllegalArgumentException, sem tocar em nenhuma dependência.</li>
 *   <li>Reserva o estoque de cada item, na ordem. Se algum não tiver estoque: libera TODOS os já reservados
 *       (com as mesmas quantidades), lança EstoqueInsuficienteException e NÃO cobra.</li>
 *   <li>Cobra o cliente pelo total (soma de quantidade × preço unitário).</li>
 *   <li>Gateway indisponível (exceção): libera todas as reservas e deixa a exceção sair. Não notifica nada.</li>
 *   <li>Pagamento recusado: libera todas as reservas, notifica pagamentoRecusado(cliente, motivo) e lança
 *       PagamentoRecusadoException. Não salva nada.</li>
 *   <li>Aprovado: salva o PedidoConfirmado (número "PED-...", total, código de autorização, confirmadoEm = relógio)
 *       e SÓ DEPOIS de salvar notifica pedidoConfirmado(cliente, número). Devolve o pedido salvo.</li>
 * </ol>
 */
public class ServicoDeCheckout implements Checkout {

    private final Estoque estoque;
    private final GatewayDePagamento gateway;
    private final RepositorioDePedidos repositorio;
    private final Notificador notificador;
    private final Clock clock;

    public ServicoDeCheckout(Estoque estoque, GatewayDePagamento gateway, RepositorioDePedidos repositorio,
            Notificador notificador, Clock clock) {
        this.estoque = estoque;
        this.gateway = gateway;
        this.repositorio = repositorio;
        this.notificador = notificador;
        this.clock = clock;
    }

    @Override
    public PedidoConfirmado finalizar(Pedido pedido) {
        validar(pedido);
        List<Item> reservados = reservar(pedido.itens());
        BigDecimal total = total(pedido.itens());
        ResultadoPagamento pagamento = cobrar(pedido.clienteId(), total, reservados);
        if (!pagamento.aprovado()) {
            recusar(pedido.clienteId(), pagamento.motivoRecusa(), reservados);
        }
        return confirmar(pedido.clienteId(), total, pagamento.codigoAutorizacao());
    }

    private void validar(Pedido pedido) {
        if (pedido == null || pedido.itens() == null || pedido.itens().isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter itens");
        }
        if (pedido.itens().stream().anyMatch(i -> i.quantidade() <= 0)) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
    }

    private List<Item> reservar(List<Item> itens) {
        List<Item> reservados = new ArrayList<>();
        for (Item item : itens) {
            if (!estoque.reservar(item.sku(), item.quantidade())) {
                liberar(reservados);
                throw new EstoqueInsuficienteException(item.sku());
            }
            reservados.add(item);
        }
        return reservados;
    }

    private void liberar(List<Item> itens) {
        for (Item item : itens) {
            estoque.liberar(item.sku(), item.quantidade());
        }
    }

    private BigDecimal total(List<Item> itens) {
        return itens.stream()
                .map(i -> i.precoUnitario().multiply(BigDecimal.valueOf(i.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ResultadoPagamento cobrar(String clienteId, BigDecimal total, List<Item> reservados) {
        try {
            return gateway.cobrar(clienteId, total);
        } catch (GatewayIndisponivelException e) {
            liberar(reservados);
            throw e;
        }
    }

    private void recusar(String clienteId, String motivo, List<Item> reservados) {
        liberar(reservados);
        notificador.pagamentoRecusado(clienteId, motivo);
        throw new PagamentoRecusadoException(motivo);
    }

    private PedidoConfirmado confirmar(String clienteId, BigDecimal total, String codigoAutorizacao) {
        var confirmado = new PedidoConfirmado(numero(), clienteId, total, codigoAutorizacao, agora());
        repositorio.salvar(confirmado);
        notificador.pedidoConfirmado(clienteId, confirmado.numero());
        return confirmado;
    }

    private String numero() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Instant agora() {
        return clock.instant();
    }
}
