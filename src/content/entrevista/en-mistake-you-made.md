---
pergunta: "Tell me about a mistake you made. What did you learn from it?"
tema: "Behavioral (EN)"
nivel: jr
idioma: en
modulo: 17-carreira
intencao: "Ownership, honesty and learning: whether you take responsibility, fix the problem and prevent it from happening again."
pontosChave:
  - "A real mistake with real (but manageable) consequences, not a disguised strength"
  - "Own it in the first person: 'I did', not 'it happened'"
  - "How you fixed it quickly and communicated it"
  - "What you changed so it doesn't happen again (a check, a test, a habit)"
armadilha: "Saying you've never made a mistake, picking a fake one ('I work too hard'), or blaming someone else."
---

"While building my portfolio project, I committed the database password to a public repository. I noticed it about a week later, when GitHub's secret scanning flagged it.

**I treated it as a leak**, because the history is public and anyone could have cloned it. First, I changed the password. Then I moved the credentials to environment variables, added a `.env` file to `.gitignore` and turned on push protection, so the same mistake gets blocked before it's pushed. I also wrote a short note in the README about how to configure secrets locally.

Nothing bad came of it, because it was a study project with no real data. But the lesson was clear: **removing the secret in a new commit doesn't fix anything; revoking it does**. Since then, secrets have been part of my review checklist, and I set up secret scanning in every new repository from day one."
