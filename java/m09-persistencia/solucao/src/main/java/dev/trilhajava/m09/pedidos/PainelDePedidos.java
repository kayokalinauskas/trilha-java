package dev.trilhajava.m09.pedidos;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PainelDePedidos {

    private final PedidoRepository pedidos;

    public PainelDePedidos(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    /** 2 comandos SQL, para qualquer quantidade: pedidos+clientes e depois itens+produtos. */
    @Transactional(readOnly = true)
    public List<ResumoDoPedido> ultimos(int quantidade) {
        List<Pedido> recentes = pedidos.recentesComCliente(Limit.of(quantidade));
        Map<Long, List<ItemPedido>> itensPorPedido = pedidos.itensComProduto(ids(recentes)).stream()
                .collect(Collectors.groupingBy(item -> item.getPedido().getId()));
        return recentes.stream()
                .map(p -> resumir(p, itensPorPedido.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    /** Até 3 comandos SQL: ids da página, contagem e uma consulta com todos os JOIN FETCH. */
    @Transactional(readOnly = true)
    public Page<ResumoDoPedido> doCliente(long clienteId, Pageable pagina) {
        Page<Long> idsDaPagina = pedidos.idsDoCliente(clienteId, pagina);
        Map<Long, Pedido> porId = pedidos.completos(idsDaPagina.getContent()).stream()
                .collect(Collectors.toMap(Pedido::getId, Function.identity()));
        // A consulta com "in" não garante ordem: seguimos a ordem da página.
        return idsDaPagina.map(id -> resumir(porId.get(id), porId.get(id).getItens()));
    }

    /** 1 comando SQL: GROUP BY no banco. */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> faturamentoPorCliente() {
        return pedidos.faturamentoPorCliente().stream()
                .collect(Collectors.toMap(FaturamentoDoCliente::cliente, FaturamentoDoCliente::total,
                        BigDecimal::add, TreeMap::new));
    }

    private static List<Long> ids(Collection<Pedido> lista) {
        return lista.stream().map(Pedido::getId).toList();
    }

    private static ResumoDoPedido resumir(Pedido pedido, List<ItemPedido> itens) {
        List<String> nomes = itens.stream().map(i -> i.getProduto().getNome()).sorted(Comparator.naturalOrder()).toList();
        BigDecimal total = itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoDoPedido(pedido.getId(), pedido.getCliente().getNome(), pedido.getCriadoEm(),
                pedido.getStatus(), itens.size(), total, nomes);
    }
}
