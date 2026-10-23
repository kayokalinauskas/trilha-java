---
pergunta: "Tell me about a project you're proud of."
tema: "Behavioral (EN)"
nivel: jr
idioma: en
modulo: 17-carreira
intencao: "Whether you can explain technical work clearly, show ownership of decisions and talk about impact, not just technologies."
pontosChave:
  - "The problem first, then the solution"
  - "Your own contribution, in the first person"
  - "One hard decision and its trade-off"
  - "A concrete result or something you measured"
  - "Keep it under two minutes and let them dig deeper"
armadilha: "Listing technologies ('Spring, Kafka, Docker, Redis...') without explaining what problem they solved or why you chose them."
---

"I'm proud of a digital wallet API I built as my portfolio project. The goal was to practice what a financial backend really needs: **correct balances under concurrency** and reliable events.

**I designed and built it on my own**, with Spring Boot, PostgreSQL and Kafka. The hardest part was a race condition: when I ran fifty transfers in parallel, the balance sometimes went negative. I reproduced it in an integration test first, then fixed it with **optimistic locking and a retry**. I chose optimistic over pessimistic locking because conflicts were rare, and it kept throughput higher.

For events, I used the **outbox pattern**, so a transfer and its event are saved in the same transaction and nothing gets lost if Kafka is down.

**The result**: the concurrency test has been running in CI ever since, and the project has over 200 tests. What makes me proud isn't the stack. It's that I can prove the balance is correct."

Pick the project you know best, not the most impressive one. The follow-up questions will go deep.
