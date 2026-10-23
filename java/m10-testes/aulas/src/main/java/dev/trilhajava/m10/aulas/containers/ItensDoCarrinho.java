package dev.trilhajava.m10.aulas.containers;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Os itens de cada carrinho, no PostgreSQL.
 *
 * <p>Adicionar um produto que já está no carrinho SOMA a quantidade. Isso é um único comando
 * atômico (upsert: INSERT ... ON CONFLICT DO UPDATE), que funciona mesmo com vários pedidos
 * chegando ao mesmo tempo para o mesmo carrinho. O H2 não entende essa sintaxe.
 */
@Repository
public class ItensDoCarrinho {

    private final JdbcClient jdbc;

    public ItensDoCarrinho(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void adicionar(UUID carrinho, String sku, int quantidade) {
        jdbc.sql("""
                INSERT INTO item_carrinho (carrinho_id, sku, quantidade)
                VALUES (:carrinho, :sku, :quantidade)
                ON CONFLICT (carrinho_id, sku)
                DO UPDATE SET quantidade    = item_carrinho.quantidade + EXCLUDED.quantidade,
                              atualizado_em = now()""")
                .param("carrinho", carrinho)
                .param("sku", sku)
                .param("quantidade", quantidade)
                .update();
    }

    /** Os itens em ordem de SKU; lista vazia se o carrinho não existe. */
    public List<ItemDoCarrinho> itens(UUID carrinho) {
        return jdbc.sql("SELECT sku, quantidade FROM item_carrinho WHERE carrinho_id = :carrinho ORDER BY sku")
                .param("carrinho", carrinho)
                .query(ItemDoCarrinho.class)
                .list();
    }

    public void remover(UUID carrinho, String sku) {
        jdbc.sql("DELETE FROM item_carrinho WHERE carrinho_id = :carrinho AND sku = :sku")
                .param("carrinho", carrinho)
                .param("sku", sku)
                .update();
    }
}
