package dev.trilhajava.m09.pedidos;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByClienteIdOrderByCriadoEmDescIdDesc(long clienteId, Pageable pageable);

    /** JOIN FETCH de um @ManyToOne: não multiplica linhas, então o LIMIT continua no banco. */
    @Query("select p from Pedido p join fetch p.cliente order by p.criadoEm desc, p.id desc")
    List<Pedido> recentesComCliente(Limit limite);

    /** Busca os itens de VÁRIOS pedidos de uma vez (com o produto junto). */
    @Query("select i from ItemPedido i join fetch i.produto where i.pedido.id in :pedidos")
    List<ItemPedido> itensComProduto(Collection<Long> pedidos);

    /** Etapa 1 da paginação: só os ids da página (paginação no banco, sem coleção envolvida). */
    @Query(value = "select p.id from Pedido p where p.cliente.id = :clienteId order by p.criadoEm desc, p.id desc",
            countQuery = "select count(p) from Pedido p where p.cliente.id = :clienteId")
    Page<Long> idsDoCliente(long clienteId, Pageable pageable);

    /** Etapa 2: tudo de que o painel precisa, para os ids da página, numa consulta só. */
    @Query("""
            select distinct p from Pedido p
              join fetch p.cliente
              join fetch p.itens i
              join fetch i.produto
             where p.id in :ids""")
    List<Pedido> completos(Collection<Long> ids);

    /** Agregação no banco: o SQL faz a soma, o Java recebe só o resultado. */
    @Query("""
            select new dev.trilhajava.m09.pedidos.FaturamentoDoCliente(c.nome, sum(i.precoUnitario * i.quantidade))
              from ItemPedido i join i.pedido p join p.cliente c
             where p.status = StatusPedido.PAGO
             group by c.nome""")
    List<FaturamentoDoCliente> faturamentoPorCliente();
}
