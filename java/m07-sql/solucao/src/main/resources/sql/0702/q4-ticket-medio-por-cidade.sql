-- Ticket é por PEDIDO: primeiro somamos cada pedido (subconsulta), depois tiramos a média por cidade.
-- Fazer AVG direto nos itens daria a média por ITEM — um erro clássico.
SELECT c.cidade,
       ROUND(AVG(t.total), 2) AS ticket_medio
  FROM (SELECT i.pedido_id, SUM(i.quantidade * i.preco_unitario) AS total
          FROM item_pedido i
         GROUP BY i.pedido_id) t
  JOIN pedido  p ON p.id = t.pedido_id
  JOIN cliente c ON c.id = p.cliente_id
 WHERE p.status = 'PAGO'
 GROUP BY c.cidade
 ORDER BY c.cidade;
