---
pergunta: "Qual a diferença entre lock otimista e pessimista? Quando usar cada um?"
tema: "JPA"
nivel: pleno
idioma: pt
modulo: 09-persistencia
intencao: "Controle de concorrência em dados — essencial em fintechs, estoque e reservas."
pontosChave:
  - "Otimista: coluna de versão; conflito detectado no commit (OptimisticLockException)"
  - "Pessimista: SELECT ... FOR UPDATE bloqueia a linha"
  - "Otimista para baixa contenção; pessimista para alta contenção no mesmo registro"
  - "Retry com limite no otimista"
  - "Alternativa: UPDATE condicional atômico"
---

"No **lock otimista**, a entidade tem uma coluna de versão (`@Version`). Ninguém bloqueia nada na leitura; na atualização, o Hibernate faz `UPDATE ... WHERE id = ? AND versao = ?`. Se outra transação atualizou antes, nenhuma linha é afetada e ele lança `OptimisticLockException`. Aí eu faço *retry* com limite ou devolvo um conflito (HTTP 409) para o usuário. É ótimo quando conflitos são **raros**: edição de cadastro, por exemplo.

No **lock pessimista**, com `@Lock(PESSIMISTIC_WRITE)`, o banco executa `SELECT ... FOR UPDATE` e as outras transações esperam. É melhor quando há **muita disputa** pelo mesmo registro, como o saldo de uma carteira muito movimentada ou o estoque de um produto em promoção. O custo é contenção, e é preciso cuidar de timeout e da ordem de aquisição para evitar deadlock.

Para casos simples, uma atualização condicional atômica, `UPDATE estoque SET qtd = qtd - 1 WHERE id = ? AND qtd > 0`, resolve sem nenhum dos dois."
