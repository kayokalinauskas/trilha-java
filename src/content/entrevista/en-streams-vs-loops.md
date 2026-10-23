---
pergunta: "When would you use a Stream instead of a for loop in Java?"
tema: "Java (EN)"
nivel: jr
idioma: en
modulo: 04-java-moderno
intencao: "Judgment about modern Java: readability first, knowing the costs and pitfalls of streams."
pontosChave:
  - "Streams for declarative pipelines: filter, map, group, collect"
  - "Loops for early exits, complex state or checked exceptions"
  - "Streams are lazy and single-use"
  - "Avoid side effects inside lambdas"
  - "Parallel streams only after measuring"
armadilha: "Saying streams are always faster, or turning everything into a long chain that nobody can debug."
---

"I choose based on **readability**. Streams are great when the code is a **pipeline of transformations**: filter the paid orders, group them by customer, sum the totals. With `filter`, `map` and `Collectors.groupingBy`, the code says *what* I want, not *how* to loop.

I prefer a **loop** when there is **complex state** or **early exits** that don't fit well, when I need to **throw checked exceptions**, or when the stream version would be a long, clever chain that's hard to debug.

Some things I keep in mind. Streams are **lazy**, so nothing runs until a terminal operation, and a stream **can't be reused**. Lambdas shouldn't have **side effects**, like adding to an external list; I use a collector for that. And **performance** is usually similar for normal workloads. I only use **parallel streams** after measuring, because for small collections or I/O work they often make things slower, and they share the common fork-join pool."
