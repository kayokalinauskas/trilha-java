---
pergunta: "How would you design a rate limiter for a public API?"
tema: "System design (EN)"
nivel: pleno
idioma: en
modulo: 15-system-design
intencao: "System design with a well-known building block: requirements, algorithm choice, distributed state and failure modes."
pontosChave:
  - "Clarify: limit per what (API key, user, IP), which limits, where it runs"
  - "Algorithms: fixed window, sliding window, token bucket and their trade-offs"
  - "Shared state in Redis with atomic operations for many instances"
  - "HTTP 429 with Retry-After and rate limit headers"
  - "Failure mode: fail open or fail closed if Redis is down"
armadilha: "Keeping counters in each instance's memory, which multiplies the real limit by the number of instances."
---

"First, some **questions**. Do we limit by API key, user or IP? Are limits different per plan, like 100 requests per minute on the free plan? Does it run in an **API gateway** or inside the service? Let's say per API key, different limits per plan, and many service instances.

For the **algorithm**:

- A **fixed window** counter is the simplest, but it allows bursts at the window boundary: up to twice the limit in a few seconds.
- A **sliding window** smooths that out.
- A **token bucket** allows controlled bursts and a steady refill rate. It's what I'd choose for a public API.

Since there are many instances, the state must be **shared**. I'd keep it in **Redis**, one key per API key, and update it **atomically** with a Lua script, so two instances can't both let the last request through. Keys get a TTL so inactive clients don't use memory.

When the limit is exceeded, I'd return **HTTP 429 Too Many Requests** with a `Retry-After` header, and rate limit headers on every response so clients can slow down.

Finally, **failure modes**. If Redis is unavailable, do we **fail open** and allow requests, or **fail closed** and reject them? For most APIs I'd fail open with a local fallback limit and an alert, because blocking every customer is worse than a short period without precise limits."
