---
pergunta: "Uma mensagem sempre falha no seu consumidor. O que acontece, e o que você faz?"
tema: "Mensageria"
nivel: pleno
idioma: pt
modulo: 12-mensageria
intencao: "Saber tratar falhas no consumo sem travar a fila nem perder mensagens."
pontosChave:
  - "Sem configuração, a mensagem volta para a fila em loop (RabbitMQ) ou trava a partição (Kafka)"
  - "Separar falhas transitórias (retry com backoff e jitter) de permanentes (direto para a DLQ)"
  - "A mensagem envenenada no deserializer do Kafka: ErrorHandlingDeserializer ou String + conversão no listener"
  - "DLQ com headers de diagnóstico, alerta e reprocessamento; consumidor idempotente"
  - "Retry bloqueante preserva a ordem; tópicos de retry não bloqueiam, mas perdem a ordem"
armadilha: "Colocar um try/catch que só registra no log: o offset avança e a mensagem some sem ninguém saber."
---

"Depende de onde ela falha, mas sem cuidado o resultado é ruim nos dois brokers. No RabbitMQ, uma exceção no listener faz a mensagem voltar para a fila, e ela é reentregue em loop. No Kafka, se a falha é no deserializer, antes do listener, o consumidor não consegue avançar o offset e **a partição inteira trava**.

O que eu faço é separar dois tipos de falha. **Transitória**, como timeout, 503 ou deadlock: repito poucas vezes, com backoff exponencial e jitter. **Permanente**, como JSON inválido ou uma regra de negócio violada: repetir não adianta, então a mensagem vai **direto** para a DLQ. No Spring Kafka, isso é um `DefaultErrorHandler` com `DeadLetterPublishingRecoverer` e `addNotRetryableExceptions`; o tópico morto, no Spring Kafka 4, termina em `-dlt`. No RabbitMQ, retry limitado no listener e uma dead letter exchange.

Para a mensagem envenenada no Kafka, uso `ErrorHandlingDeserializer`, ou recebo `String` e converto no listener. Assim o erro vira uma exceção normal e segue para o DLT.

E a DLQ não pode ser um buraco: ela tem headers com a origem e a exceção, um alerta, e um jeito de reprocessar depois da correção. Como o reprocessamento reentrega, o consumidor precisa ser idempotente."
