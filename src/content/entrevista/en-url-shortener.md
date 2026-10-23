---
pergunta: "How would you design a URL shortener? Where would you start?"
tema: "System design (EN)"
nivel: pleno
idioma: en
modulo: 15-system-design
intencao: "Structured thinking under ambiguity — the process matters more than the final diagram."
pontosChave:
  - "Clarify requirements and scale before drawing anything"
  - "Back-of-the-envelope estimates (reads vs writes, storage)"
  - "API + data model + ID generation strategy (base62, counter/snowflake vs hash)"
  - "Read-heavy: cache (Redis) and CDN; 301 vs 302"
  - "Discuss trade-offs and bottlenecks"
armadilha: "Jumping straight into microservices and Kafka before asking a single question about requirements."
---

"I'd start by **clarifying requirements**. Functional: create a short link, redirect, maybe custom aliases and expiration. Non-functional: how many new links per day, how many redirects, and what latency we need. Let's say 100 million new links per month and a **read-to-write ratio of 100 to 1**.

Then a quick **estimate**: about 40 writes per second on average, around 4,000 redirects per second, and roughly 100 GB of storage per year for links — manageable.

**API**: `POST /links` returns the short code; `GET /{code}` redirects. **Data model**: code, long URL, creation date, expiration. For **ID generation**, I'd encode a unique 64-bit ID — from a distributed generator like Snowflake, or ranges handed out by a database sequence — in **base62**. Seven characters give over 3 trillion combinations. That's simpler than hashing the URL and handling collisions.

Since it's **read-heavy**, I'd put a **Redis cache** in front of the database for hot links and use a key-value store or a partitioned relational database. Then I'd discuss **trade-offs**: a 301 redirect is cached by browsers, which reduces load but hurts click analytics, so a 302 may be better if analytics matter. Finally: rate limiting to prevent abuse, and asynchronous analytics through a queue."
