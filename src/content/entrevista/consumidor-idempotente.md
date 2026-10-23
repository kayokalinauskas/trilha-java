---
pergunta: "Como garantir que um consumidor de mensagens seja idempotente?"
tema: "Mensageria"
nivel: pleno
idioma: pt
modulo: 12-mensageria
intencao: "Consciência de que sistemas distribuídos entregam at-least-once e mensagens duplicadas são normais."
pontosChave:
  - "Brokers garantem na prática at-least-once: duplicatas acontecem"
  - "Cada evento carrega um ID único (eventId ou chave de negócio)"
  - "Registrar o ID processado na MESMA transação do efeito de negócio"
  - "Constraint de unicidade resolve a corrida entre consumidores"
  - "Operações naturalmente idempotentes quando possível (upsert)"
---

"Parto da premissa de que a mensagem **vai** chegar duplicada: em Kafka ou RabbitMQ, na prática a garantia é *at-least-once*. Um rebalanceamento, um timeout de ack ou um retry do produtor causam reentrega.

A técnica padrão: cada evento carrega um **identificador único** — um `eventId` ou uma chave de negócio, como o ID da transação. O consumidor, dentro da **mesma transação** do banco em que aplica o efeito de negócio, insere esse ID numa tabela de eventos processados com **constraint de unicidade**. Se o insert falhar por duplicidade, a mensagem já foi processada: faço o ack e sigo. Como está na mesma transação, não existe o caso de "aplicou o efeito mas não registrou o ID".

Quando dá, prefiro operações naturalmente idempotentes, como um *upsert* ("defina o status como PAGO") em vez de "some 100 ao saldo". Um cache como Redis com TTL pode servir como primeira barreira, mas a garantia forte vem do banco."
