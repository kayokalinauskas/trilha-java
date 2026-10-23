---
pergunta: "Kafka or RabbitMQ: how do you decide which one to use?"
tema: "Messaging (EN)"
nivel: pleno
idioma: en
modulo: 12-mensageria
intencao: "Reasoning about the model (queue vs log) instead of brand preference or throughput myths."
pontosChave:
  - "RabbitMQ is a queue with rich routing: a message goes to one consumer and is deleted on ack"
  - "Kafka is a partitioned, retained log: multiple groups read at their own pace and can replay"
  - "Ordering: per partition in Kafka (choose the key); per queue in RabbitMQ with a single consumer"
  - "Operational cost and team familiarity matter as much as features"
  - "The lines are blurring: RabbitMQ streams, Kafka share groups (4.2)"
armadilha: "Answering only 'Kafka is faster' — throughput is rarely the deciding factor."
---

"I start from the **model**, not the product. RabbitMQ is a **message queue**: the producer publishes to an exchange, routing rules copy the message to queues, and each message is delivered to one consumer and deleted when it's acknowledged. That's a great fit for distributing **work**: sending emails, generating PDFs, calling a slow partner API. The routing — topic exchanges, dead-letter exchanges, per-message TTL — solves a lot in configuration.

Kafka is a **partitioned, retained log**. Reading doesn't delete anything; each consumer group keeps its own offset, so the fraud team, the notification service and a data pipeline can all read the same events, and a new service can **replay** history. Ordering is guaranteed per partition, so I pick the key — say, the account id — to keep an entity's events in order. That's what I want for **streams of facts** like payments or stock movements.

Then the practical side: who is going to run it, and what the team already knows. A managed Kafka is a different cost than a small RabbitMQ cluster.

The lines are blurring, though: RabbitMQ has streams, and Kafka 4.2 made share groups production-ready, which give queue-like consumption. So I'd frame it as: work to be done once, with routing → RabbitMQ; facts that several consumers need, possibly replayed → Kafka."
