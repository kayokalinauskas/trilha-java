-- LAG olha a linha anterior na ordem da janela. No primeiro mês não há anterior: NULL.
WITH mensal AS (
    SELECT TO_CHAR(p.data, 'YYYY-MM') AS mes,
           SUM(i.quantidade * i.preco_unitario) AS faturamento
      FROM pedido p
      JOIN item_pedido i ON i.pedido_id = p.id
     WHERE p.status = 'PAGO'
     GROUP BY TO_CHAR(p.data, 'YYYY-MM')
)
SELECT mes,
       faturamento,
       faturamento - LAG(faturamento) OVER (ORDER BY mes) AS variacao
  FROM mensal
 ORDER BY mes;
