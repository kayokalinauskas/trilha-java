-- LEFT JOIN mantém todos os clientes; quem não casou com nenhum pedido fica com p.id NULL.
SELECT c.nome
  FROM cliente c
  LEFT JOIN pedido p ON p.cliente_id = c.id
 WHERE p.id IS NULL
 ORDER BY c.nome;
