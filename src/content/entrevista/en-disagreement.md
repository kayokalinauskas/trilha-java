---
pergunta: "Tell me about a time you disagreed with a teammate. How did you handle it?"
tema: "Behavioral (EN)"
nivel: pleno
idioma: en
modulo: 17-carreira
intencao: "Collaboration, humility and focus on outcomes — very important for remote international teams."
pontosChave:
  - "STAR structure"
  - "Disagree about ideas, not people"
  - "Use data or a quick experiment to decide"
  - "Disagree and commit once a decision is made"
  - "What you learned"
---

Use **STAR** and show that you focus on the outcome, not on being right:

"**Situation**: in a code review, a teammate wanted to cache product prices in memory to fix a slow endpoint. **Task**: I was worried about stale prices across multiple instances, but I didn't want to block the fix. **Action**: I asked questions first to understand the constraint — the endpoint had a strict latency target. Then I suggested we measure: we traced the request and found that 80% of the time was an N+1 query, not the price lookup. We agreed to fix the query first and add a short-lived Redis cache only if we still needed it. **Result**: latency dropped from 900 ms to 120 ms without the cache, and we avoided a consistency problem. My teammate later suggested adding query-count assertions to our tests.

What I learned: data settles disagreements faster than opinions, and once a decision is made, I commit to it even if it wasn't my first choice."
