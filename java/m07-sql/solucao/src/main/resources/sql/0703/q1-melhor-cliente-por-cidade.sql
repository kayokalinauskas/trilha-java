-- 1) CTE com o total pago por cliente; 2) RANK() numera dentro de cada cidade; 3) filtra o primeiro.
-- (Não dá para usar a window function direto no WHERE: ela é calculada DEPOIS do WHERE.)
WITH totais AS (
    SELECT c.cidade, c.nome, SUM(i.quantidade * i.preco_unitario) AS total
      FROM cliente c
      JOIN pedido p      ON p.cliente_id = c.id AND p.status = 'PAGO'
      JOIN item_pedido i ON i.pedido_id = p.id
     GROUP BY c.id, c.cidade, c.nome
), ranqueados AS (
    SELECT cidade, nome, total,
           RANK() OVER (PARTITION BY cidade ORDER BY total DESC) AS posicao
      FROM totais
)
SELECT cidade, nome, total
  FROM ranqueados
 WHERE posicao = 1
 ORDER BY cidade;
