# Fintech Core — o seu projeto integrador

Este é o projeto que você vai evoluir ao longo dos módulos 08 a 12 e 14.
Cada marco tem um enunciado no site e uma bateria de **testes de aceitação** em `../testes`,
que só falam HTTP e JSON com a sua aplicação: a organização interna é decisão sua.

| Marco | Módulo | Rode |
|---|---|---|
| 1 — API de contas | 08 | `mvn test -Dtest=Marco1ContasTest` |
| 2 — Transferências atômicas | 09 | `mvn test -Dtest='Marco1*,Marco2*'` |
| 3 — Suíte de testes | 10 | `mvn test` (os seus testes + `Marco3*`) |
| 4 — Autenticação | 11 | `mvn test` |
| 5 — Eventos (outbox + Kafka) | 12 | `mvn test` (o `Marco5EventosTest` precisa de Docker) |
| 6 — Pronto para produção | 14 | `mvn test` |

Sugestão de pacotes (obrigatória a partir do marco 3, verificada com ArchUnit):

```text
dev.trilhajava.fintech
├── api          controllers, DTOs de entrada/saída, tratamento de erros
├── aplicacao    casos de uso (serviços), orquestra domínio e repositórios
├── dominio      entidades, regras de negócio, interfaces de repositório
└── infra        implementações técnicas (persistência, configuração, integrações)
```

Rode a aplicação com `mvn spring-boot:run` e explore em `http://localhost:8080/docs`.
