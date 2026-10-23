-- Faturamento = soma de quantidade x preço praticado (preco_unitario do item, não o preço atual do produto).
SELECT pr.categoria,
       SUM(i.quantidade * i.preco_unitario) AS faturamento
  FROM item_pedido i
  JOIN pedido  p  ON p.id  = i.pedido_id
  JOIN produto pr ON pr.id = i.produto_id
 WHERE p.status = 'PAGO'
 GROUP BY pr.categoria
 ORDER BY faturamento DESC;
