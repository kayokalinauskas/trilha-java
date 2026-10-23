---
pergunta: "How do you decide what to test, and with which kind of test?"
tema: "Testing (EN)"
nivel: jr
idioma: en
modulo: 10-testes
intencao: "Whether you have a testing strategy based on risk and cost, and not just 'aim for 100% coverage'."
pontosChave:
  - "Test behavior and risk, not implementation details"
  - "Unit tests for domain rules: fast and many"
  - "Integration tests with real dependencies (Testcontainers) for SQL, transactions and messaging"
  - "A few end-to-end tests for critical flows"
  - "Coverage is a signal, not a goal"
armadilha: "Mocking everything, including the database, and then being surprised when the SQL fails in production."
---

"I start from **risk**: what would hurt most if it broke? In a payments service, that's balance calculations, idempotency and authorization, so those get the most careful tests.

Then I pick the cheapest test that gives real confidence:

- **Unit tests** for domain rules, like fee calculations or state transitions. They're fast, so I write many of them, including edge cases like zero, negative values and rounding.
- **Integration tests** for anything that talks to infrastructure. I use **Testcontainers** with the same database as production, because an in-memory database can hide SQL and transaction problems. I also test the web layer's contract, meaning status codes, validation and error bodies.
- **A few end-to-end tests** for the most critical flows, like 'create an account and transfer money'.

I test **behavior through public APIs**, not private methods, so I can refactor without rewriting tests. And I use coverage to **find untested areas**, not as a target. 100% coverage with weak assertions proves very little."
