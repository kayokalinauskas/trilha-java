---
pergunta: "Tell me about a time you had to deliver something under a tight deadline."
tema: "Behavioral (EN)"
nivel: pleno
idioma: en
modulo: 17-carreira
intencao: "Prioritization, communication and judgment: whether you cut scope wisely instead of cutting quality in silence."
pontosChave:
  - "STAR structure, with the constraint made clear"
  - "Negotiated scope: what shipped and what was postponed"
  - "Communicated risks early, not on the last day"
  - "Did not skip tests or safety for critical paths"
  - "Result and what you would do differently"
armadilha: "Telling a hero story about working all weekend, which suggests poor planning and an unhealthy pattern."
---

"**Situation**: two weeks before a partner launch, we learned that the partner needed a webhook to notify them of payment status changes. It wasn't in our plan. **Task**: I owned the integration.

**Action**: on the first day, I split the work into what the partner needed for launch and what could come later. The must-haves were sending signed notifications and retrying on failure. **Nice-to-haves**, like a dashboard to replay events, could wait. I wrote that down and **shared it with my manager and the partner the same day**, so nobody was surprised later. I kept the tests for signature validation and retries, because a bug there would mean lost payments.

**Result**: we launched on time with the core flow, and the replay dashboard came two sprints later. In hindsight, I would have asked about external integrations earlier in planning. Now I always add a question about that to our kickoff checklist."
