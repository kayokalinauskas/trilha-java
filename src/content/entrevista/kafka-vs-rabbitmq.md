---
pergunta: "Kafka ou RabbitMQ? Como você escolhe?"
tema: "Mensageria"
nivel: pleno
idioma: pt
modulo: 12-mensageria
intencao: "Capacidade de comparar tecnologias por trade-offs, não por moda."
pontosChave:
  - "RabbitMQ: broker de filas com roteamento flexível; mensagem removida após o consumo"
  - "Kafka: log distribuído, retenção, reprocessamento e alto throughput"
  - "Ordenação por partição (chave) no Kafka"
  - "RabbitMQ bom para comandos/tarefas; Kafka para streams de eventos e múltiplos consumidores"
  - "Kafka 4 roda só em KRaft (sem ZooKeeper)"
---

"São modelos diferentes. O **RabbitMQ** é um broker de filas: tem roteamento flexível com exchanges (direct, topic, fanout), a mensagem sai da fila quando é consumida e confirmada, e é ótimo para **distribuir tarefas** entre workers, com prioridades, TTL e dead letter queues simples de configurar.

O **Kafka** é um **log distribuído** e particionado: as mensagens ficam retidas por um tempo configurável, independentemente de terem sido lidas, e cada consumer group mantém seu próprio *offset*. Isso permite que vários sistemas consumam o mesmo fluxo de eventos, reprocessar o histórico e atingir throughput muito alto. A ordem é garantida **dentro de uma partição**, então uso a chave (por exemplo, o ID da conta) para manter a ordem dos eventos de uma mesma entidade. Desde o Kafka 4, ele roda só em modo KRaft, sem ZooKeeper, o que simplificou bastante a operação.

Minha regra: comandos e filas de trabalho → RabbitMQ; eventos de domínio que vários serviços consomem, *event sourcing* ou analytics em tempo real → Kafka. E pesa muito o que o time já opera bem."
