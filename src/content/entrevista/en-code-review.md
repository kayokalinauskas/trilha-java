---
pergunta: "What do you look for when you review someone else's code?"
tema: "Engineering practices (EN)"
nivel: jr
idioma: en
modulo: 14-devops
intencao: "Whether you review for what matters (correctness, risk, clarity) and give feedback in a way that helps the team, especially in async remote work."
pontosChave:
  - "Understand the goal first: the description, the ticket, the tests"
  - "Correctness and edge cases before style"
  - "Security, transactions, concurrency and error handling"
  - "Tests that prove the behavior"
  - "Kind, specific comments; separate blocking issues from suggestions"
armadilha: "Focusing only on formatting and naming, which a linter should handle, or writing vague comments like 'this is wrong'."
---

"First, I **understand the goal**: I read the description and the ticket, and I look at the tests, because they tell me what the author meant the code to do.

Then I review in order of **importance**:

1. **Correctness**: does it do what it should? What happens with empty input, nulls, duplicates, or when an external call fails?
2. **Risk**: security issues like missing authorization checks or SQL built by string concatenation, transaction boundaries, concurrency, and whether a database migration is safe for a zero-downtime deploy.
3. **Tests**: do they prove the behavior, including the failure cases?
4. **Clarity**: will someone understand this in six months? Names, structure, unnecessary complexity.

Formatting should be automated, so I don't spend comments on it.

How I write comments matters too, especially on remote teams where everything is async. I'm **specific** and explain **why**. I label suggestions as optional ('nit:'), and I ask questions instead of making accusations: 'What happens if this list is empty?' And I approve good work quickly, because a PR waiting for days is a cost too."
