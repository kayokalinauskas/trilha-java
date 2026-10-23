-- WHERE filtra LINHAS antes de agrupar; HAVING filtra GRUPOS depois de agrupar.
SELECT c.nome,
       COUNT(*) AS pedidos_pagos
  FROM cliente c
  JOIN pedido p ON p.cliente_id = c.id
 WHERE p.status = 'PAGO'
 GROUP BY c.id, c.nome
HAVING COUNT(*) >= 2
 ORDER BY pedidos_pagos DESC, c.nome;
