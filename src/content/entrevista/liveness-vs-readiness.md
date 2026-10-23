---
pergunta: "Qual a diferença entre liveness e readiness? O banco deve entrar em qual delas?"
tema: "DevOps"
nivel: pleno
idioma: pt
modulo: 14-devops
intencao: "Saber o que o orquestrador faz com cada probe, e o estrago que uma probe mal escolhida causa em produção."
pontosChave:
  - "Liveness falhou: o Kubernetes reinicia o container"
  - "Readiness falhou: o pod sai do Service (para de receber tráfego), sem reiniciar"
  - "Dependência externa não entra na liveness: reiniciar não conserta o banco de outro servidor"
  - "Na readiness, só o que impede de atender; um Kafka fora, com outbox, não impede"
  - "No Spring Boot: /actuator/health/liveness e /readiness, grupos configuráveis"
armadilha: "Colocar o banco na liveness. Quando o banco oscila, todos os pods reiniciam juntos, e a aplicação cai inteira por causa de uma falha que já ia passar."
---

"As duas respondem perguntas diferentes, e o orquestrador reage de forma diferente a cada uma.

A **liveness** pergunta: *este processo está travado?* Se ela falha, o Kubernetes **reinicia** o container. Ela deve olhar só para dentro da aplicação: o estado interno está bom, a JVM não entrou num deadlock. A **readiness** pergunta: *consigo atender agora?* Se ela falha, o pod é **tirado do Service** e para de receber tráfego, mas não é reiniciado. É o que acontece durante o startup, enquanto o cache aquece, ou num modo de manutenção.

Por isso, dependência externa **nunca** vai na liveness. Se o PostgreSQL oscila e ele está na liveness, todos os pods reiniciam ao mesmo tempo. O reinício não conserta o banco, e a aplicação ainda perde as requisições em andamento e demora para subir de novo. Na readiness, o banco pode entrar: sem ele, a API não atende mesmo. Já algo como o Kafka, num sistema com outbox, eu deixo fora das duas. A transferência continua funcionando e o evento espera na tabela. Esse caso vai para o health completo e para um alerta, por exemplo quando o evento pendente mais antigo passa de 10 minutos.

No Spring Boot 4, as probes vêm ligadas em `/actuator/health/liveness` e `/actuator/health/readiness`. Eu escolho o que entra em cada uma com `management.endpoint.health.group.readiness.include`. Para tirar uma instância do ar sem matá-la, publico um `AvailabilityChangeEvent` com `ReadinessState.REFUSING_TRAFFIC`."
