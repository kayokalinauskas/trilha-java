---
pergunta: "Você começaria um sistema novo com microsserviços ou com um monólito? Por quê?"
tema: "Arquitetura"
nivel: pleno
idioma: pt
modulo: 13-arquitetura
intencao: "Avaliar critério: decidir por fatos (time, carga, requisitos) e conhecer o monólito modular como meio-termo."
pontosChave:
  - "O motivo principal de microsserviços é autonomia de times; o custo é a rede (latência, falhas, consistência eventual, observabilidade)"
  - "Para times pequenos e produtos em validação, monólito modular com fronteiras verificadas"
  - "Separar desde o início só o que tem requisitos muito diferentes (latência, escala, disponibilidade)"
  - "Fronteiras por contexto de negócio; eventos como contrato; banco por módulo"
  - "Registrar a decisão num ADR, com gatilhos de reavaliação"
armadilha: "Responder por moda ('todo mundo usa microsserviços') ou por aversão, sem olhar o contexto."
---

"Depende do contexto, e eu consigo dizer do quê. Microsserviços resolvem principalmente um problema **organizacional**: vários times entregando partes diferentes sem coordenar deploys. O preço é a rede. Cada chamada pode falhar ou demorar, a consistência entre serviços vira eventual, e cada serviço precisa de pipeline, banco e observabilidade.

Para um sistema novo com um time de até uns dez devs, eu começaria com um **monólito modular**: módulos por contexto de negócio, com fronteiras verificadas no CI. Uso o Spring Modulith para isso: nenhum módulo usa os pacotes internos de outro, e não há ciclos. A comunicação entre módulos é por eventos, com o registro de publicações para não perder nada. Assim tenho deploy e transações simples, e o caminho fica aberto: extrair um módulo como serviço é trocar o evento interno por um evento no Kafka.

A exceção é a parte com requisitos **muito diferentes** do resto: uma autorização de pagamento com latência baixa e pico concentrado, por exemplo. Essa eu separo desde o início.

E registro a decisão num ADR, com gatilhos mensuráveis para revisar: número de times, conflitos de deploy, tempo de build."
