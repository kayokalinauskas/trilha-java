---
pergunta: "Para que serve um circuit breaker? E como ele se relaciona com retry e timeout?"
tema: "Arquitetura"
nivel: pleno
idioma: pt
modulo: 13-arquitetura
intencao: "Resiliência em sistemas distribuídos."
pontosChave:
  - "Timeout sempre: nunca esperar indefinidamente"
  - "Retry com backoff e jitter, só para falhas transitórias e operações idempotentes"
  - "Circuit breaker: após muitas falhas, abre e falha rápido; meio-aberto testa a recuperação"
  - "Fallback quando fizer sentido"
  - "Resilience4j no ecossistema Spring"
---

"São três camadas. **Timeout** primeiro: toda chamada remota precisa de um limite, senão threads e conexões ficam presas esperando um serviço travado. **Retry** para falhas transitórias, com *backoff* exponencial e *jitter*, e só para operações idempotentes — senão um retry pode cobrar duas vezes.

O **circuit breaker** protege contra uma dependência que está falhando de forma persistente. Ele monitora a taxa de falhas e de lentidão; se passa de um limite, **abre** o circuito e as chamadas falham imediatamente, sem gastar recursos e dando fôlego para a dependência se recuperar. Depois de um tempo, fica **meio-aberto** e deixa algumas chamadas de teste passarem; se funcionarem, fecha. Enquanto está aberto, posso usar um **fallback**: um valor em cache, uma resposta degradada, ou colocar o pedido numa fila.

No ecossistema Spring, uso o **Resilience4j**, que também tem *bulkhead* e *rate limiter*. Sem isso, a lentidão de um serviço vira falha em cascata no sistema inteiro."
