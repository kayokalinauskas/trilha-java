---
pergunta: "Como funcionam as Streams? Qual a diferença entre map e flatMap?"
tema: "Java moderno"
nivel: jr
idioma: pt
modulo: 04-java-moderno
intencao: "Uso idiomático de Streams e entendimento de avaliação preguiçosa."
pontosChave:
  - "Pipeline: fonte → operações intermediárias → operação terminal"
  - "Operações intermediárias são lazy; nada roda sem a terminal"
  - "map transforma 1 → 1; flatMap transforma 1 → N e achata"
  - "Streams não são reutilizáveis; evite efeitos colaterais"
---

"Uma Stream é um pipeline: uma **fonte** (uma coleção, por exemplo), zero ou mais **operações intermediárias** como `filter` e `map`, e uma **operação terminal** como `collect`, `sum` ou `findFirst`. As intermediárias são **preguiçosas**: nada é processado até a terminal ser chamada, e aí cada elemento passa pelo pipeline inteiro. Isso permite otimizações como parar no primeiro resultado em `findFirst`.

`map` transforma cada elemento em exatamente um outro: pedidos em totais. `flatMap` transforma cada elemento em uma **stream** de elementos e achata tudo em uma só: por exemplo, de uma lista de pedidos para todos os itens de todos os pedidos, `pedidos.stream().flatMap(p -> p.itens().stream())`.

Cuidados: uma Stream não pode ser consumida duas vezes, e evito efeitos colaterais dentro de `map` ou `filter`, como adicionar em uma lista externa. Para agrupar, uso `Collectors.groupingBy`."
