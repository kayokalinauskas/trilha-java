# Curadoria técnica — edição 2026

Registro das versões adotadas, do que mudou em relação à versão 1 do projeto e das fontes consultadas. Revisado em **setembro de 2026**.

## Versões adotadas

| Tecnologia | Versão | Por quê |
|---|---|---|
| Java | **25 (LTS, set/2025)** | LTS atual. O 21 continua muito presente no mercado; diferenças são sinalizadas nas aulas |
| Spring Boot | **4.1** (4.1.1 no projeto, jun/2026) | Linha atual; construída sobre Spring Framework 7, Jakarta EE 11 e Jackson 3. Spring Boot não tem versões LTS: cada minor recebe 12 meses de suporte OSS |
| Spring Framework | 7.0 (7.0.9) | `RestClient`, HTTP interfaces com `@ImportHttpServices`, versionamento de API nativo |
| Spring Security | 7.1 (7.1.1) | DSL só com lambdas; autoridades de fator (`FACTOR_PASSWORD`...) para MFA; `BCryptPasswordEncoder` recusa senhas acima de 72 bytes |
| Spring Data JPA / Hibernate ORM | 4.1 / 7.4 | Acompanham o Boot 4 |
| Jackson | 3.1 (pacote `tools.jackson`) | Acompanha o Boot 4 (as anotações continuam em `com.fasterxml.jackson.annotation`) |
| Flyway | 12 | Migrações versionadas desde o módulo 09 |
| springdoc-openapi | 3.1 | OpenAPI/Swagger UI para o Boot 4 |
| Nimbus JOSE + JWT | 10.9 | Base do `NimbusJwtEncoder`/`NimbusJwtDecoder` do Spring Security |
| ArchUnit / JaCoCo | 1.5 / 0.8.15 | Arquitetura como teste e cobertura (módulo 10) |
| PostgreSQL | 18 | Banco de referência (Docker Compose e Testcontainers) |
| JUnit | 6 (6.1.x nos módulos 00–07; 6.0.x pelo BOM do Boot nos módulos Spring) | Lançado em set/2025; exige Java 17+ |
| AssertJ | 3.27.x | Linha estável (a 4.x ainda é milestone) |
| H2 | 2.5.x (só nos testes do módulo 07) | Banco em memória no modo PostgreSQL, para os exercícios de SQL rodarem sem Docker. Todas as soluções foram conferidas também num PostgreSQL 18 real |
| Testcontainers | 2 (2.0.x) | Usado a partir do módulo 10, com PostgreSQL de verdade (`@ServiceConnection`, `@EnabledIfDockerAvailable`) |
| Apache Kafka | 4 (4.2, imagem `apache/kafka`) | Somente KRaft; ZooKeeper removido. Spring Kafka 4.1 (tópico de DLT padrão: `<tópico>-dlt`) |
| RabbitMQ / Spring AMQP | 4 / 4.1 | Filas, exchanges e DLQ no módulo 12 |
| Spring Modulith | 2.1 | Monólito modular verificado em teste (módulo 13) |
| Resilience4j | 2.4 | Circuit breaker programático (módulo 13); retry e limite de concorrência nativos do Spring Framework 7 (`@Retryable`, `@ConcurrencyLimit`) |
| Micrometer / Prometheus | 1.17 | Métricas de negócio e `/actuator/prometheus` (módulo 14, marco 6) |
| Logs estruturados | nativos do Boot (desde 3.4) | `logging.structured.format.console=ecs` (módulo 14) |
| Spring AI | 2.0 (2.0.1, BOM `spring-ai-bom`) | Chat, saída estruturada, RAG e tool calling no módulo 16. Os testes usam `ChatModel` e `EmbeddingModel` falsos, sem chave de API nem rede |
| Docker / imagem base | `eclipse-temurin:25-jre-noble` | Multi-stage com extração em camadas (`-Djarmode=tools`) |
| Redis | 8 | Cache-aside, locks e rate limit (módulo 15). Licença AGPLv3 a partir do Redis 8; Valkey como fork |
| MongoDB / Spring Data MongoDB | 8 / 5.1 (release train 2026.0) | Documentos, índices e agregações (módulo 15) |
| Maven | 3.9 | Maven 4 ainda em adoção |

## O que mudou em relação à v1 do projeto

| v1 (antiga) | Edição 2026 | Motivo |
|---|---|---|
| Java 17/21 como alvo | Java 25 LTS | Nova LTS |
| Spring Boot 3.x | Spring Boot 4.x | Nova geração (Framework 7, Jakarta EE 11) |
| "RFC 7807 (ProblemDetail)" | **RFC 9457** | A RFC 9457 substituiu a 7807 em 2023; o `ProblemDetail` do Spring segue a nova |
| `@MockBean` | `@MockitoBean` | `@MockBean` foi descontinuado no Spring Boot 3.4 |
| `RestTemplate` | `RestClient` e HTTP interfaces | APIs recomendadas atualmente |
| "Kafka com ZooKeeper ou KRaft" | Somente KRaft | Kafka 4 removeu o ZooKeeper |
| Virtual threads: "evite `synchronized` por causa do pinning" | Pinning com `synchronized` resolvido no **Java 24 (JEP 491)** | Recomendação desatualizada |
| Outbox "garante entrega 100% confiável" | Outbox garante **pelo menos uma vez** | Consumidores idempotentes continuam obrigatórios |
| OWASP Top 10 **2021** | OWASP Top 10 **2025** (+ API Security Top 10 2023) | Nova edição: entram *Software Supply Chain Failures* e *Mishandling of Exceptional Conditions*; SSRF passa a fazer parte de *Broken Access Control* |
| Spring Security 6 | Spring Security 7: DSL só com lambdas (sem `.and()`), autoridades de fator para MFA | Nova geração, junto com o Boot 4 |
| JWT com a biblioteca jjwt e segredo no código | OAuth2 Resource Server (`NimbusJwtDecoder`) com algoritmo e emissor validados, segredo por variável de ambiente | Suporte nativo do Spring Security e boas práticas da RFC 8725 |
| Senhas: "BCrypt e pronto" | BCrypt/Argon2id, política do NIST SP 800-63B rev. 4, limite de 72 bytes do BCrypt | Recomendações atuais |
| Começava em POO | Módulos 00 e 01 de ponte para quem sabe só o básico | Pedido da nova proposta |
| Sem algoritmos, HTTP, OWASP, OpenTelemetry, IA | Módulos 05, 08, 11, 14 e 16 | Temas cobrados em processos atuais |
| Exercícios sem solução | Solução testada no CI para cada exercício | Nova proposta |
| Foco exclusivo em São Paulo, salários sem fonte | Brasil, remoto e internacional; números só com fonte e data | Precisão |
| Títulos do catálogo divergentes do conteúdo | Catálogo gerado a partir do conteúdo | Consistência |
| Actuator: "ligue as probes com `management.endpoint.health.probes.enabled`" | No Boot 4, liveness e readiness vêm ligadas por padrão | Mudança de padrão |
| Spring Data MongoDB grava `BigDecimal` como texto | Grava como `Decimal128` por padrão (e o curso configura explicitamente) | Faixas numéricas corretas |

## Recursos do Java citados na trilha

| Recurso | Versão final | Onde aparece |
|---|---|---|
| Records | 16 | Módulo 02 |
| Sealed classes | 17 | Módulo 02 |
| Pattern matching no switch, record patterns | 21 | Módulo 02 |
| Sequenced Collections | 21 | Módulo 03 |
| Virtual threads | 21 | Módulo 06 |
| Unnamed variables (`_`) | 22 | Módulo 02 (nota) |
| Stream Gatherers | 24 | Módulo 04 |
| `synchronized` sem pinning em virtual threads (JEP 491) | 24 | Módulo 06 |
| Compact source files e `void main()` (JEP 512) | 25 | Módulo 00 |
| Flexible constructor bodies (JEP 513) | 25 | Módulo 02 |
| Scoped values (JEP 506) | 25 | Módulo 06 |
| Structured concurrency | preview | Módulo 06 |

## Fontes

- OpenJDK — páginas dos JEPs e notas de lançamento (openjdk.org).
- Spring — blog de lançamentos e documentação de referência (spring.io).
- RFC 9457 — *Problem Details for HTTP APIs* (rfc-editor.org).
- OWASP — Top 10:2025, API Security Top 10 (2023) e Cheat Sheet Series (owasp.org).
- NIST SP 800-63B, revisão 4 — *Digital Identity Guidelines: Authentication* (nist.gov).
- RFC 7519 (JWT), RFC 8725 (JWT BCP) e RFC 9700 (OAuth 2.0 Security BCP).
- roadmap.sh — trilhas "Java" e "Backend" (referência de cobertura de tópicos).
- Exercism — formato de exercícios com testes.

## Política de revisão

- **A cada release semestral do Java** (março e setembro): revisar os selos "Java moderno" e a tabela acima.
- **A cada minor do Spring Boot** (maio/novembro): revisar os módulos 08–15 e as versões do projeto de exemplo.
- **A cada minor do Spring AI**: revisar o módulo 16 (a API ainda muda bastante entre versões).
- **Números de mercado** (salários, vagas) só entram com **fonte e data** e são revisados a cada 6 meses.
