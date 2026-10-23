---
pergunta: "You added an index, but the query is still slow. What could be going on?"
tema: "Databases (EN)"
nivel: jr
idioma: en
modulo: 07-sql
intencao: "Whether you know how to investigate with EXPLAIN and understand when an index can't be used."
pontosChave:
  - "Check the plan with EXPLAIN ANALYZE instead of guessing"
  - "A function or cast on the column prevents the index (unless it's an expression index)"
  - "LIKE with a leading wildcard; wrong column order in a composite index"
  - "Low selectivity: a sequential scan can be cheaper"
  - "Outdated statistics; the problem may be elsewhere (N+1, locks, too many rows)"
armadilha: "Adding more indexes at random, which also slows down every write."
---

"First, I'd **look at the execution plan** with `EXPLAIN ANALYZE` instead of guessing. It tells me whether the index is used, how many rows are estimated compared to the actual rows, and where the time goes.

Common reasons the index isn't used:

- **A function on the column**, like `WHERE lower(email) = ?` or `WHERE date(created_at) = ?`. The index is on the raw column, so I'd rewrite the condition as a range or create an **expression index**.
- **`LIKE '%term'`** with a leading wildcard, which a regular B-tree index can't use.
- **Column order in a composite index**: an index on `(customer_id, created_at)` doesn't help a query that filters only by `created_at`.
- **Low selectivity**: if the query returns a large part of the table, a sequential scan is actually cheaper, and the planner is right.
- **Outdated statistics**, which lead to bad estimates. Running `ANALYZE` can fix them.

And sometimes the query itself is fine, but it's executed a thousand times because of an **N+1 problem**, or it's **waiting on a lock**. That's why I measure before changing anything."
