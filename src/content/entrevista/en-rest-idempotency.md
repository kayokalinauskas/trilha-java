---
pergunta: "What does idempotency mean in REST APIs, and why does it matter?"
tema: "HTTP (EN)"
nivel: jr
idioma: en
modulo: 08-spring-boot
intencao: "API design fundamentals, explained in English."
pontosChave:
  - "Same effect whether the request is sent once or many times"
  - "GET, PUT, DELETE are idempotent; POST is not"
  - "Networks fail and clients retry"
  - "Idempotency-Key header for payments"
---

"An operation is **idempotent** if performing it multiple times has the same effect as performing it once. It matters because networks are unreliable: a client might time out and **retry** a request that actually succeeded.

By definition, `GET`, `PUT` and `DELETE` are idempotent. `PUT` replaces the whole resource, so sending it twice leaves the same final state. `POST` is not: two identical POSTs can create two orders or charge a card twice.

For critical operations like payments, the common pattern is an **`Idempotency-Key`** header: the client sends a unique ID with the request, and the server stores the key together with the result. If the same key arrives again, the server returns the stored result instead of processing the payment a second time."
