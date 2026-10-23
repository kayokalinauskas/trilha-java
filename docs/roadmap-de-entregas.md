# Roadmap de entregas

| Fase | Escopo | Status |
|---|---|---|
| **1** | Plataforma completa (mapa, aulas, exercícios, dicas, solução trancada, busca, flashcards, simulador, painel, PWA, CI/deploy) + módulos **00–03** + 48 perguntas de entrevista | ✅ Entregue |
| **2** | Módulos **04** (Java moderno), **05** (algoritmos), **06** (concorrência), **07** (SQL) — 23 aulas, 18 exercícios com testes e soluções | ✅ Entregue |
| **3** | Módulos **08** (Spring Boot 4), **09** (JPA), **10** (testes), **11** (segurança) + marcos 1–4 do projeto integrador **Fintech Core** — 24 aulas, 20 exercícios (4 deles são os marcos), 217 testes nas soluções dos módulos e 180 no projeto integrador | ✅ Entregue |
| **4** | Módulos **12** (mensageria), **13** (arquitetura), **14** (DevOps e observabilidade), **15** (cache, NoSQL e system design) + marcos 5 (eventos com outbox e Kafka) e 6 (produção) do **Fintech Core** — 24 aulas, 20 exercícios (2 deles são os marcos), 168 testes nas soluções dos módulos e 210 no projeto integrador | ✅ Entregue |
| **5** | Módulos **16** (IA no backend, com Spring AI 2 testado sem chave de API) e **17** (carreira) + simulador internacional ampliado (95 perguntas, 34 em inglês, e o formato "loop completo") + revisão final + remoção do `legado/` — 12 aulas, 10 exercícios, 68 testes na solução do módulo 16 | ✅ Entregue |

## Como a v1 foi reaproveitada

As especificações da v1 (em `especificacoes-md/`, no commit `b5c453d`) foram a matéria-prima dos exercícios:

| Legado | Destino |
|---|---|
| 2-1 a 2-4 (Streams, Optional, records, concorrência) | Módulos 04 e 06 |
| 3-1 a 3-4 (Spring) | Módulo 08 |
| 4-1 a 4-4 (JPA, N+1, locking, Flyway) | Módulo 09 |
| 5-x (segurança) | Módulo 11 |
| 6-x (testes) | Módulo 10 |
| 7-x (mensageria) | Módulo 12 |
| 8-x (arquitetura) | Módulo 13 |
| 9-x (DevOps) | Módulo 14 |
| 10 (projeto integrador) | Marcos do Fintech Core |
| 11-x (mercado) | Módulo 17 (com dados revisados e fontes) |
| 12-x, 13-x (cloud, NoSQL, system design) | Módulos 14 e 15 |

Os exercícios 1-1 a 1-4 da v1 já foram migrados, revisados e ganharam solução (02.02, 03.01, 03.03 e 03.04). Na fase 2, as ideias de 2-1 a 2-4 viraram os exercícios 04.01–04.04 e 06.01–06.04. Na fase 3, os temas de 3-x, 4-x, 5-x e 6-x (Spring, JPA, segurança e testes) foram reescritos como os módulos 08–11, e o projeto integrador da v1 virou os marcos 1–4 do Fintech Core, cada um com testes de aceitação caixa-preta em `java/projeto-integrador/testes`. Na fase 4, 7-x, 8-x e 9-x viraram os módulos 12–14, 12-x e 13-x viraram o módulo 15, e os marcos 5 e 6 fecharam o Fintech Core com eventos e com a entrega em produção. Na fase 5, 11-x virou o módulo 17, com a política de só citar números de mercado com fonte e data. Com tudo migrado, a pasta `legado/` foi removida; a v1 continua no histórico do git.

## Depois da fase 5

A trilha está completa. Daqui em diante, a manutenção segue a [política de revisão da curadoria](curadoria-2026.md#política-de-revisão): versões a cada release do Java e do Spring Boot, números de mercado a cada 6 meses.
