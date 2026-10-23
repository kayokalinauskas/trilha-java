---
pergunta: "You're adding an LLM-powered feature to a Java backend. What do you need to think about before it goes to production?"
tema: "AI in the backend (EN)"
nivel: pleno
idioma: en
modulo: 16-ia-backend
intencao: "Treating an LLM as an unreliable, costly external dependency, with the same engineering rigor as any other."
pontosChave:
  - "Treat the model like any external dependency: timeouts, retries, circuit breaker, fallback"
  - "Validate structured output; never let model text trigger actions directly"
  - "Security: prompt injection, least-privilege tools, identity from the auth context"
  - "Privacy: minimize and mask personal data (LGPD/GDPR), check provider data retention"
  - "Cost and observability: token budgets, per-user rate limits, token and latency metrics, evals"
---

"I treat the model as an **external dependency that is slow, expensive and sometimes wrong**. That changes a few things.

**Reliability:** every call gets a timeout, a retry for transient errors like 429s, and a circuit breaker with a sensible fallback, so the feature degrades instead of taking the page down. I ask for **structured output**, for example with Spring AI's `entity()`, and validate it like any external input. If it's invalid, I retry once, then fall back.

**Security:** prompt injection is unavoidable, so I limit the blast radius. Instructions go in the system prompt, and user content is delimited. Tools get the user's identity from the application context, never from an argument the model fills in. Anything irreversible, like moving money, becomes a proposal the user confirms. The model never triggers an action just by writing some text.

**Privacy:** I send the minimum data, aggregated where possible, and mask personal data before it leaves the company. I also check the provider's retention and training terms, because in Brazil that's LGPD, and in Europe, GDPR.

**Cost and observability:** token usage and latency go into metrics. Spring AI emits them through Micrometer. I also set per-user rate limits and a budget alert. Finally, I keep an **eval set** of known inputs and expected answers, and I run it before changing the model or the prompt, so quality regressions show up as numbers, not as customer complaints."
