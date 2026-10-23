---
pergunta: "Quando um índice ajuda e quando ele não ajuda (ou atrapalha)?"
tema: "SQL"
nivel: jr
idioma: pt
modulo: 07-sql
intencao: "Base de SQL para performance."
pontosChave:
  - "Índice acelera leitura seletiva (WHERE, JOIN, ORDER BY)"
  - "Custo: escrita mais lenta e espaço"
  - "Não ajuda: baixa seletividade, função na coluna, LIKE com % no início"
  - "Índice composto: ordem das colunas importa"
  - "Validar com EXPLAIN (ANALYZE)"
---

"Um índice (em geral uma B-tree) permite ao banco achar linhas sem ler a tabela inteira. Ele ajuda em filtros **seletivos** no `WHERE`, em colunas de `JOIN` e em `ORDER BY`.

Ele tem custo: cada `INSERT`, `UPDATE` e `DELETE` precisa atualizar os índices, e eles ocupam espaço. E há casos em que não ajuda: colunas com **baixa seletividade** (um `status` com 3 valores, em que metade das linhas é `ATIVO`); **funções sobre a coluna** (`WHERE lower(email) = ?` não usa o índice de `email`, a não ser que eu crie um índice de expressão); e `LIKE '%termo'` com curinga no início.

Em índices compostos, a **ordem** importa: um índice `(cliente_id, criado_em)` serve para filtrar por cliente, ou por cliente e data, mas não só por data. Nunca adivinho: rodo `EXPLAIN ANALYZE` no PostgreSQL e confirmo se o plano usa o índice e quanto tempo leva."
