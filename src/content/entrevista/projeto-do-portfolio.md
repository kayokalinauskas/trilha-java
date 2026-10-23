---
pergunta: "Me fale de um projeto do seu portfólio. O que você faria diferente hoje?"
tema: "Carreira"
nivel: jr
idioma: pt
modulo: 17-carreira
intencao: "Verificar se você entende o que construiu (decisões, trade-offs, limites) e se tem senso crítico sobre o próprio trabalho."
pontosChave:
  - "Dois minutos: problema, arquitetura em uma frase, uma decisão difícil, um bug, o que faria diferente"
  - "Explicar o porquê das escolhas e o trade-off, não só listar tecnologias"
  - "Mostrar como verificou (testes, carga, CI)"
  - "Crítica honesta ao próprio projeto, com a melhoria concreta"
armadilha: "Listar tecnologias sem explicar decisões, ou não saber responder sobre uma parte do próprio repositório."
---

"O projeto principal é uma API de conta digital em Spring Boot 4, com PostgreSQL, Kafka e Docker. A ideia era praticar o que um backend financeiro exige: saldo correto sob concorrência, eventos confiáveis e segurança por cliente.

Uma decisão difícil foi como publicar os eventos de transferência. Publicar direto no Kafka dentro da transação arriscava perder eventos, ou publicar eventos de transferências que depois eram desfeitas. Usei o padrão **outbox**: o evento é gravado na mesma transação e um agendador publica depois. O custo é um pouco de latência e a entrega 'pelo menos uma vez', então o consumidor precisa ser idempotente.

O bug mais interessante foi um *lost update*: com 50 transferências simultâneas, o saldo ficava negativo. Resolvi com lock otimista e nova tentativa, e esse teste roda no CI desde então.

O que eu faria diferente: começaria com os testes de concorrência desde o primeiro dia, e colocaria **observabilidade** mais cedo, com métricas e tracing. Quando o outbox atrasava, eu só percebia olhando o banco, e num sistema real isso precisaria de um alerta."
