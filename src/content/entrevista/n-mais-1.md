---
pergunta: "Como você diagnostica e resolve o problema de N+1 queries no Hibernate?"
tema: "JPA"
nivel: pleno
idioma: pt
modulo: 09-persistencia
intencao: "Experiência prática com JPA e impacto em performance de produção."
pontosChave:
  - "1 query para a lista + N queries para as associações"
  - "Diagnóstico: log de SQL, contagem de statements, tracing"
  - "Solução: JOIN FETCH, @EntityGraph ou projeções/DTO"
  - "Manter LAZY por padrão; EAGER não resolve"
  - "Cuidado com paginação + fetch de coleções"
armadilha: "Responder \"troco LAZY por EAGER\" — o N+1 continua e ainda carrega dados desnecessários em todo lugar."
---

"O N+1 acontece quando busco uma lista de N entidades com uma query e, ao acessar uma associação *lazy* de cada uma, o Hibernate dispara mais uma query por entidade. Uma tela de pedidos com 100 pedidos vira 101 queries.

Para **diagnosticar**, ativo o log de SQL em desenvolvimento, verifico a contagem de statements (as estatísticas do Hibernate ajudam) e, em produção, olho os traces do OpenTelemetry, onde aparece uma "escadinha" de queries iguais.

Para **resolver**, mantenho os relacionamentos `LAZY` e, na consulta que precisa dos dados, busco tudo de uma vez: `JOIN FETCH` no JPQL ou `@EntityGraph` no repositório do Spring Data. Se a tela só precisa de alguns campos, uma **projeção** para DTO é ainda melhor. Um cuidado: `JOIN FETCH` de coleção com paginação faz o Hibernate paginar em memória; nesse caso, pagino os IDs primeiro e busco as associações em uma segunda query, ou uso `@BatchSize`. Trocar para `EAGER` não resolve: o N+1 continua e passa a acontecer em todo lugar."
