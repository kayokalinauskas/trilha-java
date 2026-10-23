-- A tabela de pedidos só tem os dias COM venda; para achar os dias SEM venda, geramos o calendário.
-- A CTE recursiva conta de 0 a 6 e soma ao dia inicial (date + integer = date, no PostgreSQL e no H2).
WITH RECURSIVE deslocamento (n) AS (
    SELECT 0
    UNION ALL
    SELECT n + 1 FROM deslocamento WHERE n < 6
), calendario (dia) AS (
    SELECT DATE '2026-03-01' + n FROM deslocamento
)
SELECT c.dia
  FROM calendario c
 WHERE NOT EXISTS (SELECT 1 FROM pedido p WHERE p.data = c.dia AND p.status = 'PAGO')
 ORDER BY c.dia;
