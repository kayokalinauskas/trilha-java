---
pergunta: "Por que usar Testcontainers em vez de banco em memória (H2) nos testes de integração?"
tema: "Testes"
nivel: pleno
idioma: pt
modulo: 10-testes
intencao: "Maturidade de engenharia: fidelidade ao ambiente de produção."
pontosChave:
  - "H2 tem dialeto e comportamento diferentes do PostgreSQL"
  - "Recursos específicos: JSONB, funções, constraints, locks"
  - "Testcontainers sobe o banco real em Docker"
  - "@ServiceConnection no Spring Boot simplifica a configuração"
  - "Custo: mais lento; mitigado com reuso de containers"
---

"Um teste de integração só vale se testa o que vai para produção. O H2 fala um dialeto parecido, mas não igual ao do PostgreSQL: tipos como `JSONB`, funções específicas, comportamento de locks, algumas constraints e até migrações do Flyway podem passar no H2 e falhar em produção — ou o contrário, e aí o teste acusa um erro que não existe.

Com **Testcontainers**, o teste sobe um PostgreSQL real, na mesma versão de produção, dentro de um container Docker, e o descarta ao final. No Spring Boot, com `@ServiceConnection`, a configuração da conexão é automática. Faço o mesmo para Kafka, Redis e RabbitMQ.

O custo é tempo de inicialização. Mitigo reutilizando um container por suíte (um container estático compartilhado) e separando os testes: unitários rápidos rodando a cada mudança, e integração com containers no `mvn verify` e no CI."
