package dev.trilhajava.m09.pedidos;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * O resultado esperado é calculado com SQL puro (JdbcTemplate), que não passa pelo Hibernate e,
 * portanto, não entra na contagem de comandos das estatísticas.
 */
@SpringBootTest
@DisplayName("Exercício 09.02 — Painel de pedidos sem N+1")
class PainelDePedidosTest {

    @Autowired
    PainelDePedidos painel;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory emf;

    Statistics estatisticas;

    @BeforeEach
    void zerar() {
        estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();
    }

    long comandosSql() {
        return estatisticas.getPrepareStatementCount();
    }

    /** O "gabarito": o resumo de cada pedido montado por SQL. */
    Map<Long, ResumoDoPedido> esperado(String where, Object... args) {
        Map<Long, ResumoDoPedido> resumos = new LinkedHashMap<>();
        jdbc.query("""
                select p.id, c.nome as cliente, p.criado_em, p.status
                  from pedido p join cliente c on c.id = p.cliente_id
                """ + where + " order by p.criado_em desc, p.id desc", (RowCallbackHandler) rs -> {
            long id = rs.getLong("id");
            List<String> produtos = jdbc.queryForList("""
                    select pr.nome from item_pedido i join produto pr on pr.id = i.produto_id
                     where i.pedido_id = ? order by pr.nome""", String.class, id);
            BigDecimal total = jdbc.queryForObject(
                    "select sum(quantidade * preco_unitario) from item_pedido where pedido_id = ?", BigDecimal.class, id);
            resumos.put(id, new ResumoDoPedido(id, rs.getString("cliente"),
                    rs.getObject("criado_em", java.time.OffsetDateTime.class).toInstant(),
                    StatusPedido.valueOf(rs.getString("status")), produtos.size(), total, produtos));
        }, args);
        return resumos;
    }

    static void mesmoConteudo(List<ResumoDoPedido> obtidos, List<ResumoDoPedido> esperados) {
        assertThat(obtidos).hasSameSizeAs(esperados);
        for (int i = 0; i < esperados.size(); i++) {
            ResumoDoPedido o = obtidos.get(i);
            ResumoDoPedido e = esperados.get(i);
            assertThat(o.id()).as("id na posição %d", i).isEqualTo(e.id());
            assertThat(o.cliente()).isEqualTo(e.cliente());
            assertThat(o.criadoEm()).isEqualTo(e.criadoEm());
            assertThat(o.status()).isEqualTo(e.status());
            assertThat(o.itens()).as("itens do pedido %d", e.id()).isEqualTo(e.itens());
            assertThat(o.total()).as("total do pedido %d", e.id()).isEqualByComparingTo(e.total());
            assertThat(o.produtos()).as("produtos do pedido %d", e.id()).isEqualTo(e.produtos());
        }
    }

    @ParameterizedTest(name = "últimos {0} pedidos")
    @ValueSource(ints = {5, 25})
    @DisplayName("últimos pedidos: dados corretos em no máximo 2 comandos SQL, qualquer que seja a quantidade")
    void ultimos(int quantidade) {
        List<ResumoDoPedido> obtidos = painel.ultimos(quantidade);
        long comandos = comandosSql();

        mesmoConteudo(obtidos, new ArrayList<>(esperado("").values()).subList(0, quantidade));
        assertThat(comandos).as("comandos SQL para %d pedidos (o N+1 daria dezenas)", quantidade).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("pedidos do cliente: paginados NO BANCO, em no máximo 3 comandos SQL")
    void doCliente() {
        Page<ResumoDoPedido> pagina = painel.doCliente(3, PageRequest.of(1, 2));
        long comandos = comandosSql();
        long pedidosCarregados = estatisticas.getEntityStatistics(Pedido.class.getName()).getLoadCount();

        List<ResumoDoPedido> todos = new ArrayList<>(esperado("where p.cliente_id = ?", 3L).values());
        mesmoConteudo(pagina.getContent(), todos.subList(2, 4));
        assertThat(pagina.getTotalElements()).isEqualTo(todos.size());
        assertThat(pagina.getNumber()).isEqualTo(1);
        assertThat(comandos).as("comandos SQL").isLessThanOrEqualTo(3);
        assertThat(pedidosCarregados).as("pedidos carregados (paginar em memória carregaria todos)").isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("faturamento por cliente (só pedidos PAGOS): agregado pelo banco em 1 comando SQL")
    void faturamento() {
        Map<String, BigDecimal> obtido = painel.faturamentoPorCliente();
        long comandos = comandosSql();

        Map<String, BigDecimal> esperado = new LinkedHashMap<>();
        jdbc.query("""
                select c.nome, sum(i.quantidade * i.preco_unitario) as total
                  from item_pedido i join pedido p on p.id = i.pedido_id join cliente c on c.id = p.cliente_id
                 where p.status = 'PAGO' group by c.nome order by c.nome""",
                rs -> {
                    esperado.put(rs.getString("nome"), rs.getBigDecimal("total"));
                });

        assertThat(obtido.keySet()).containsExactlyElementsOf(esperado.keySet());
        esperado.forEach((cliente, total) -> assertThat(obtido.get(cliente)).as(cliente).isEqualByComparingTo(total));
        assertThat(comandos).as("comandos SQL").isEqualTo(1);
    }

    @Test
    @DisplayName("o painel continua correto para um cliente com um único pedido na página")
    void ultimaPagina() {
        List<ResumoDoPedido> todos = new ArrayList<>(esperado("where p.cliente_id = ?", 5L).values());
        int ultima = (todos.size() - 1) / 3;
        Page<ResumoDoPedido> pagina = painel.doCliente(5, PageRequest.of(ultima, 3));
        long comandos = comandosSql();

        mesmoConteudo(pagina.getContent(), todos.subList(ultima * 3, todos.size()));
        assertThat(pagina.isLast()).isTrue();
        assertThat(comandos).as("comandos SQL").isLessThanOrEqualTo(3);
        assertThat(Instant.EPOCH).isBefore(pagina.getContent().getFirst().criadoEm());
    }
}
