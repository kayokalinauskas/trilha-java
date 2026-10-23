-- A window function roda sobre o resultado JÁ agrupado por dia: SUM(SUM(...)) OVER (...) é válido.
SELECT p.data,
       SUM(i.quantidade * i.preco_unitario)                            AS faturamento_dia,
       SUM(SUM(i.quantidade * i.preco_unitario)) OVER (ORDER BY p.data) AS acumulado
  FROM pedido p
  JOIN item_pedido i ON i.pedido_id = p.id
 WHERE p.status = 'PAGO'
 GROUP BY p.data
 ORDER BY p.data;
