---
pergunta: "What are virtual threads, and when would you use them?"
tema: "Concurrency (EN)"
nivel: pleno
idioma: en
modulo: 06-concorrencia
intencao: "Up-to-date Java knowledge and the ability to reason about trade-offs."
pontosChave:
  - "Lightweight threads managed by the JVM (Java 21+)"
  - "Blocking I/O unmounts the virtual thread from its carrier"
  - "Great for I/O-bound, thread-per-request servers; no gain for CPU-bound work"
  - "Don't pool them; limit scarce resources with semaphores/pools"
---

"Virtual threads, final since **Java 21**, are lightweight threads managed by the JVM instead of the operating system. A platform thread maps one-to-one to an OS thread and is expensive, so servers usually cap them at a few hundred. Virtual threads cost a few kilobytes, so you can have **millions**.

The key mechanism: when a virtual thread blocks on I/O — a database query, an HTTP call — the JVM **unmounts** it from its carrier thread, which becomes free to run other virtual threads. So you keep the simple thread-per-request, blocking style of code, but get throughput close to reactive code for **I/O-bound** workloads. In Spring Boot, it's one property: `spring.threads.virtual.enabled=true`.

They don't help with **CPU-bound** work, because you're still limited by the number of cores. And you shouldn't pool them: create one per task, and if you need to limit access to a scarce resource, like database connections, use a semaphore or rely on the connection pool."
