---
pergunta: "Tell me about a time you had to learn a new technology quickly."
tema: "Behavioral (EN)"
nivel: jr
idioma: en
modulo: 17-carreira
intencao: "How you learn: whether you have a method, use primary sources and deliver something useful while still learning."
pontosChave:
  - "Why you needed it and the time you had"
  - "Your learning method: official docs, a small spike, asking for help"
  - "How you checked that you understood it (tests, a working prototype)"
  - "What you delivered, and how you shared what you learned"
armadilha: "Saying 'I watched a course' without showing how you applied it, or claiming you mastered something in two days."
---

"When I started the messaging module of my portfolio project, I had never used **Kafka**, and I wanted the feature working in about a week.

I started with the **official documentation** and the Spring for Apache Kafka reference, not random tutorials, because versions change a lot. Then I built a **small spike**: one producer, one consumer, running in Docker. I broke it on purpose, stopping the consumer and sending a message that couldn't be processed, to see what happened.

That showed me what I didn't know yet: offsets, retries and dead-letter topics. **I wrote integration tests with Testcontainers** for those cases before adding Kafka to the real project.

In the end, I delivered the feature in the planned time, and I wrote a short README section explaining the retry and DLT setup. That note helped me later, and in a team it would help the next person."
