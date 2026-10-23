---
pergunta: "What is the N+1 query problem, and how do you fix it in JPA?"
tema: "JPA (EN)"
nivel: pleno
idioma: en
modulo: 09-persistencia
intencao: "Whether you understand what the ORM does behind the scenes and know how to detect and fix it."
pontosChave:
  - "One query for the list, plus one query per item for a lazy association"
  - "How to detect it: SQL logs, Hibernate statistics, query-count assertions in tests"
  - "Fixes: JOIN FETCH, @EntityGraph, DTO projection, batch fetching"
  - "Careful with fetch joins on collections and pagination"
  - "Why switching to EAGER is not the fix"
armadilha: "Answering 'just use FetchType.EAGER', which loads data you don't need everywhere and often causes N+1 anyway."
---

"The **N+1 problem** happens when you load a list of entities with one query and then access a lazy association on each of them. Hibernate runs **one more query per item**. For 100 orders, you get 1 query for the orders and 100 queries for their customers. It's easy to miss in development with ten rows, and slow in production with thousands.

To **detect it**, I look at the SQL logs or Hibernate statistics. Better yet, I **assert the number of queries in a test**, so it can't come back silently.

To **fix it**, the main option is loading what I need in one query. I can use **`JOIN FETCH`** in JPQL, or an **`@EntityGraph`** on the repository method. If the endpoint only needs a few fields, a **DTO projection** is even better, because it avoids loading full entities. Another option is **batch fetching**, with `@BatchSize` or `hibernate.default_batch_fetch_size`. It turns N queries into a few `IN` queries.

One caveat: a fetch join on a **collection** combined with **pagination** makes Hibernate paginate in memory. In that case, I paginate the IDs first and then fetch the associations for that page."
