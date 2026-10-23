package dev.trilhajava.m10.aulas.containers;

import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * A PRIMEIRA versão do carrinho, que o time testou só no H2: "tenta inserir; se o produto
 * já está no carrinho, soma". No H2 funciona. No PostgreSQL, o erro de chave duplicada
 * aborta a transação inteira, e o UPDATE do catch nunca executa. Não copie este código:
 * ele existe para a aula mostrar o teste que passa no H2 e falha no banco real.
 */
@Repository
public class ItensDoCarrinhoV1 {

    private final JdbcClient jdbc;

    public ItensDoCarrinhoV1(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void adicionar(UUID carrinho, String sku, int quantidade) {
        try {
            jdbc.sql("INSERT INTO item_carrinho (carrinho_id, sku, quantidade) VALUES (:carrinho, :sku, :quantidade)")
                    .param("carrinho", carrinho)
                    .param("sku", sku)
                    .param("quantidade", quantidade)
                    .update();
        } catch (DuplicateKeyException produtoJaEstaNoCarrinho) {
            jdbc.sql("""
                    UPDATE item_carrinho
                       SET quantidade = quantidade + :quantidade, atualizado_em = CURRENT_TIMESTAMP
                     WHERE carrinho_id = :carrinho AND sku = :sku""")
                    .param("carrinho", carrinho)
                    .param("sku", sku)
                    .param("quantidade", quantidade)
                    .update();
        }
    }

    public List<ItemDoCarrinho> itens(UUID carrinho) {
        return jdbc.sql("SELECT sku, quantidade FROM item_carrinho WHERE carrinho_id = :carrinho ORDER BY sku")
                .param("carrinho", carrinho)
                .query(ItemDoCarrinho.class)
                .list();
    }
}
