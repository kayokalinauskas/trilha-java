---
pergunta: "O que é observabilidade? Como você investigaria uma API que ficou lenta?"
tema: "DevOps"
nivel: pleno
idioma: pt
modulo: 14-devops
intencao: "Capacidade de operar sistemas em produção, não só de escrever código."
pontosChave:
  - "Três sinais: logs, métricas e traces"
  - "Logs estruturados (JSON) com correlation/trace id"
  - "Métricas RED (rate, errors, duration) e percentis (p95/p99)"
  - "Tracing distribuído com OpenTelemetry"
  - "Investigar do sintoma à causa: métricas → traces → logs"
---

"Observabilidade é a capacidade de entender o que está acontecendo **dentro** do sistema a partir do que ele emite. Os três sinais são: **métricas** (números agregados ao longo do tempo, como taxa de requisições, erros e latência), **logs** (eventos detalhados, de preferência estruturados em JSON e com um *trace id*) e **traces** (o caminho de uma requisição por todos os serviços e chamadas). No Spring Boot, uso Micrometer e OpenTelemetry, com Actuator exposto para o Prometheus.

Para uma API lenta: começo pelas **métricas**. A latência subiu em todos os endpoints ou em um? No p50 ou só no p99? Coincidiu com um deploy ou com aumento de tráfego? Depois vou para os **traces** das requisições lentas e vejo onde o tempo está: numa query, numa chamada HTTP externa, esperando conexão do pool. Aí os **logs** daquele trace id dão o detalhe. Causas frequentes: N+1, índice faltando, pool de conexões esgotado, dependência externa lenta sem timeout, ou pausas de GC."
