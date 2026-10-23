-- NOT EXISTS lida bem com NULLs e costuma ter plano melhor que NOT IN com subconsulta.
SELECT pr.nome
  FROM produto pr
 WHERE NOT EXISTS (SELECT 1
                     FROM item_pedido i
                     JOIN pedido p ON p.id = i.pedido_id
                    WHERE i.produto_id = pr.id
                      AND p.status = 'PAGO')
 ORDER BY pr.nome;
