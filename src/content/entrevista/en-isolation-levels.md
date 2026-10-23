---
pergunta: "What are transaction isolation levels? Which one would you use for a money transfer?"
tema: "Databases (EN)"
nivel: pleno
idioma: en
modulo: 07-sql
intencao: "Whether you connect isolation levels to the anomalies they prevent, and choose based on the use case instead of memorizing a table."
pontosChave:
  - "The four levels and the anomalies: dirty read, non-repeatable read, phantom read"
  - "PostgreSQL's default is Read Committed"
  - "Higher isolation means more conflicts and retries"
  - "For a transfer: Read Committed plus row locks or optimistic locking, or Serializable with retries"
  - "Lost update is the real risk in a read-modify-write flow"
armadilha: "Saying 'always use Serializable' without mentioning retries and throughput, or ignoring the lost update problem."
---

"Isolation levels define **what a transaction can see from other concurrent transactions**. From weakest to strongest:

- **Read Uncommitted** allows *dirty reads*, meaning you can see data that may be rolled back. PostgreSQL treats it as Read Committed.
- **Read Committed** only shows committed data, but reading the same row twice can return different values. That's a *non-repeatable read*. It's the **default in PostgreSQL**.
- **Repeatable Read** gives you a consistent snapshot for the whole transaction. In PostgreSQL it also prevents *phantom reads*.
- **Serializable** makes the result equivalent to running transactions one at a time. The database may abort a transaction with a serialization error, so **the application must retry**.

For a **money transfer**, the real danger is a **lost update**: two transactions read the same balance, both subtract, and one write overwrites the other. I'd keep Read Committed and protect the rows explicitly. That means `SELECT ... FOR UPDATE`, locking accounts in a consistent order to avoid deadlocks, or **optimistic locking** with a version column and a retry. Serializable also works, but it needs retry logic everywhere and can reduce throughput under contention. Whatever I choose, I'd prove it with a **concurrency test** against a real database."
