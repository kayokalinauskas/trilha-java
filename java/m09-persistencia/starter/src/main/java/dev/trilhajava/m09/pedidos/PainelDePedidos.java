package dev.trilhajava.m09.pedidos;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercício 09.02 — este painel FUNCIONA: devolve os dados certos.
 * Mas ele dispara dezenas de consultas (o problema N+1) e a página do painel leva 6 segundos
 * em produção. Reescreva os três métodos para usar pouquíssimas consultas, mantendo o resultado.
 *
 * Dica para enxergar o problema: descomente spring.jpa.show-sql em pedidos.properties e rode os testes.
 */
@Service
public class PainelDePedidos {

    private final PedidoRepository pedidos;

    public PainelDePedidos(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    /** Os N pedidos mais recentes (de qualquer status). Meta: no máximo 2 comandos SQL. */
    @Transactional(readOnly = true)
    public List<ResumoDoPedido> ultimos(int quantidade) {
        Sort ordem = Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id"));
        return pedidos.findAll(PageRequest.of(0, quantidade, ordem)).stream().map(this::resumir).toList();
    }

    /** Pedidos de um cliente, do mais recente ao mais antigo. Meta: no máximo 3 comandos SQL, paginando NO BANCO. */
    @Transactional(readOnly = true)
    public Page<ResumoDoPedido> doCliente(long clienteId, Pageable pagina) {
        return pedidos.findByClienteIdOrderByCriadoEmDescIdDesc(clienteId, pagina).map(this::resumir);
    }

    /** Total dos pedidos PAGOS por nome de cliente, em ordem alfabética. Meta: 1 comando SQL. */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> faturamentoPorCliente() {
        Map<String, BigDecimal> total = new TreeMap<>();
        for (Pedido pedido : pedidos.findAll()) {
            if (pedido.getStatus() == StatusPedido.PAGO) {
                BigDecimal valor = pedido.getItens().stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                total.merge(pedido.getCliente().getNome(), valor, BigDecimal::add);
            }
        }
        return total;
    }

    private ResumoDoPedido resumir(Pedido pedido) {
        List<ItemPedido> itens = pedido.getItens();                  // 1 consulta por pedido
        List<String> nomes = itens.stream()
                .map(item -> item.getProduto().getNome())             // 1 consulta por produto ainda não carregado
                .sorted(Comparator.naturalOrder())
                .toList();
        BigDecimal total = itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoDoPedido(pedido.getId(), pedido.getCliente().getNome(), // 1 consulta por cliente
                pedido.getCriadoEm(), pedido.getStatus(), itens.size(), total, nomes);
    }
}
