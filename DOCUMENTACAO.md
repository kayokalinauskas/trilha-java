# Documentação da Trilha Java Backend

> Arquivo **gerado automaticamente** por `npm run docs:catalogo` a partir de `src/content/`. Não edite à mão.
> Para a visão geral do projeto, veja o [README](README.md). Para escrever conteúdo novo, veja [docs/guia-de-autoria.md](docs/guia-de-autoria.md).

## Visão geral

| Item | Quantidade |
|---|---|
| Módulos no currículo | 18 (18 publicados) |
| Aulas publicadas | 113 (~131h de leitura e prática guiada) |
| Exercícios com solução | 82 (54 com testes para o aluno, 20 com starter e solução em Java sem testes prontos, 8 escritos) |
| Projetos de portfólio | 9 (3 Júnior, 3 Pleno, 3 Sênior) |
| Flashcards | 674 |
| Perguntas do simulador de entrevista | 95 (34 em inglês) |

## Currículo

| # | Módulo | Nível | Horas | Status | Pré-requisitos |
|---|---|---|---|---|---|
| 00 | **Comece aqui** | Base | ~4h | ✅ publicado | — |
| 01 | **Java essencial** | Base | ~10h | ✅ publicado | 00 |
| 02 | **POO na prática** | Júnior | ~12h | ✅ publicado | 01 |
| 03 | **Coleções, Generics e Exceções** | Júnior | ~12h | ✅ publicado | 02 |
| 04 | **Java moderno e funcional** | Júnior | ~10h | ✅ publicado | 03 |
| 05 | **Algoritmos para entrevistas** | Júnior | ~16h | ✅ publicado | 04 |
| 06 | **Concorrência** | Pleno | ~12h | ✅ publicado | 04 |
| 07 | **Bancos relacionais e SQL** | Júnior | ~12h | ✅ publicado | 03 |
| 08 | **HTTP, REST e Spring Boot 4** | Júnior | ~16h | ✅ publicado | 04, 07 |
| 09 | **Persistência com JPA** | Jr → Pleno | ~14h | ✅ publicado | 08 |
| 10 | **Testes automatizados** | Jr → Pleno | ~22h | ✅ publicado | 08 |
| 11 | **Segurança** | Pleno | ~26h | ✅ publicado | 09 |
| 12 | **Mensageria e EDA** | Pleno | ~26h | ✅ publicado | 09, 10 |
| 13 | **Arquitetura** | Pleno | ~24h | ✅ publicado | 09, 10 |
| 14 | **DevOps e observabilidade** | Jr → Pleno | ~12h | ✅ publicado | 10 |
| 15 | **Cache, NoSQL e System Design** | Pleno | ~14h | ✅ publicado | 12, 13 |
| 16 | **IA no backend** | Jr → Pleno | ~8h | ✅ publicado | 08 |
| 17 | **Carreira e entrevistas** | Todos | ~8h | ✅ publicado | 05, 15 |

### 00 · Comece aqui

Como estudar com esta trilha, preparar o ambiente (JDK 25, IntelliJ, Maven, Git) e rodar seu primeiro programa e seu primeiro teste.

**Objetivos:**

- Entender o mapa da trilha e o método de estudo ativo (fazer antes de ver a solução)
- Instalar e verificar JDK 25, Maven, Git e IntelliJ IDEA
- Executar um programa Java pelo terminal e pela IDE
- Rodar os testes de um exercício da trilha e ler o resultado

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Como esta trilha funciona | 20 min | 3 |
| 2 | Preparando o ambiente | 45 min | 5 |
| 3 | Seu primeiro programa (do jeito moderno e do jeito clássico) | 35 min | 4 |
| 4 | Git essencial e seu primeiro teste automatizado | 50 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 00.01 | Seu primeiro teste verde | 🟢 Fácil | 30–45 min | `java/m00-comece-aqui` · testes: `SaudacaoTest` |

### 01 · Java essencial

A ponte entre 'sei programar o básico' e 'escrevo Java de verdade': tipos, controle de fluxo, métodos, arrays, Strings e depuração.

**Objetivos:**

- Escolher o tipo certo (int, long, double, BigDecimal, boolean, char, String)
- Escrever métodos pequenos, com nomes claros e sem efeitos colaterais escondidos
- Manipular arrays e Strings com segurança, entendendo imutabilidade
- Depurar um programa com breakpoints em vez de adivinhar

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Tipos, variáveis e o problema do dinheiro | 55 min | 6 |
| 2 | Operadores e decisões (if, switch moderno e ternário) | 45 min | 5 |
| 3 | Laços sem medo (for, while e o erro de um) | 40 min | 4 |
| 4 | Métodos pequenos, nomes claros | 50 min | 5 |
| 5 | Arrays, Strings e StringBuilder | 55 min | 5 |
| 6 | Depuração e leitura de erros | 45 min | 4 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 01.01 | Validador de CPF | 🟢 Fácil | 1–1,5h | `java/m01-java-essencial` · testes: `ValidadorCpfTest` |
| 01.02 | Simulador de parcelamento | 🟡 Médio | 1,5–2h | `java/m01-java-essencial` · testes: `ParcelamentoTest` |
| 01.03 | Relatório da estação meteorológica | 🟢 Fácil | 1h | `java/m01-java-essencial` · testes: `EstacaoMeteorologicaTest` |
| 01.04 | Gerador de slugs para URLs | 🟡 Médio | 1,5h | `java/m01-java-essencial` · starter + solução |

### 02 · POO na prática

Modelar o mundo em objetos que protegem suas regras: encapsulamento, composição, interfaces, polimorfismo, records e sealed types.

**Objetivos:**

- Criar classes que garantem suas invariantes desde o construtor
- Decidir entre herança, composição e interfaces com argumentos
- Usar polimorfismo para eliminar cadeias de if/instanceof
- Modelar dados imutáveis com record e hierarquias fechadas com sealed

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Classes, objetos e referências | 45 min | 5 |
| 2 | Encapsulamento e invariantes | 50 min | 5 |
| 3 | Herança versus composição | 50 min | 5 |
| 4 | Interfaces e polimorfismo | 50 min | 5 |
| 5 | Records, enums e objetos imutáveis | 55 min | 5 |
| 6 | Sealed types e pattern matching | 55 min | 6 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 02.01 | Conta corrente com cheque especial | 🟢 Fácil | 1,5–2h | `java/m02-poo` · testes: `ContaCorrenteTest` |
| 02.02 | Orçamento de adesivos da gráfica | 🟡 Médio | 2–3h | `java/m02-poo` · testes: `OrcamentoTest` |
| 02.03 | Value object Dinheiro | 🟡 Médio | 2h | `java/m02-poo` · testes: `DinheiroTest` |
| 02.04 | Frete com sealed types e pattern matching | 🟡 Médio | 2h | `java/m02-poo` · testes: `CalculadoraFreteTest` |

### 03 · Coleções, Generics e Exceções

As estruturas que você usa todo dia no backend: List, Set, Map e seus custos, equals/hashCode, generics seguros e tratamento de erros profissional.

**Objetivos:**

- Escolher a coleção certa sabendo o custo (Big-O) de cada operação
- Implementar equals/hashCode corretamente e explicar por que importa
- Escrever APIs genéricas com bounded types e wildcards (PECS)
- Projetar uma hierarquia de exceções de negócio e usar try-with-resources

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | List, Set, Map e companhia | 50 min | 5 |
| 2 | O custo de cada coleção (Big-O na prática) | 50 min | 5 |
| 3 | equals, hashCode, Comparable e Comparator | 50 min | 5 |
| 4 | Generics sem medo (e o tal do PECS) | 55 min | 5 |
| 5 | Exceções como parte do design | 60 min | 6 |
| 6 | Coleções modernas e utilitários | 45 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 03.01 | Consolidação do catálogo de peças | 🟢 Fácil | 1,5–2h | `java/m03-colecoes-generics-excecoes` · testes: `ConsolidadorCatalogoTest` |
| 03.02 | Ranking de produtos mais vendidos | 🟡 Médio | 1,5h | `java/m03-colecoes-generics-excecoes` · testes: `RankingVendasTest` |
| 03.03 | Exceções no sistema de empréstimos da biblioteca | 🟡 Médio | 2–3h | `java/m03-colecoes-generics-excecoes` · testes: `BibliotecaTest` |
| 03.04 | Métricas genéricas de sensores IoT | 🟡 Médio | 2h | `java/m03-colecoes-generics-excecoes` · testes: `MetricasGenericasTest` |
| 03.05 | Cache LRU com operações O(1) | 🔴 Difícil | 2–3h | `java/m03-colecoes-generics-excecoes` · testes: `CacheLruTest` |

### 04 · Java moderno e funcional

Lambdas, Streams, Collectors, Stream Gatherers, Optional bem usado e pattern matching com records e switch.

**Objetivos:**

- Transformar laços imperativos em pipelines de Stream legíveis
- Agrupar e agregar dados com Collectors
- Usar Optional como tipo de retorno sem anti-patterns
- Modelar regras com switch + record patterns

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Lambdas e interfaces funcionais | 50 min | 5 |
| 2 | Streams do jeito certo | 55 min | 6 |
| 3 | Collectors — agrupar, contar e resumir | 50 min | 5 |
| 4 | Optional sem anti-padrões | 40 min | 5 |
| 5 | Java moderno na prática (Gatherers, pattern matching e dados imutáveis) | 45 min | 4 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 04.01 | Painel analítico de vendas com Streams | 🟡 Médio | 2h | `java/m04-java-moderno` · testes: `PainelVendasTest` |
| 04.02 | Diretório de funcionários com Optional | 🟢 Fácil | 1–1,5h | `java/m04-java-moderno` · testes: `DiretorioTest` |
| 04.03 | Motor de regras de crédito com funções | 🟡 Médio | 1,5–2h | `java/m04-java-moderno` · testes: `MotorCreditoTest` |
| 04.04 | Projeção de conta a partir de eventos | 🔴 Difícil | 2–3h | `java/m04-java-moderno` · testes: `ProjecaoContaTest` |

### 05 · Algoritmos para entrevistas

Os padrões de problemas que mais aparecem em entrevistas no Brasil e fora: Big-O, two pointers, sliding window, hashing, recursão, BFS/DFS.

**Objetivos:**

- Estimar complexidade de tempo e espaço de uma solução
- Reconhecer o padrão de um problema a partir do enunciado
- Comunicar o raciocínio em voz alta durante o live coding

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Como resolver um problema de entrevista | 45 min | 4 |
| 2 | Hashing e dois ponteiros | 50 min | 4 |
| 3 | Janela deslizante | 45 min | 4 |
| 4 | Pilhas e filas | 45 min | 4 |
| 5 | Busca binária e ordenação | 55 min | 5 |
| 6 | Grafos, BFS, DFS e intervalos | 60 min | 6 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 05.01 | Auditoria de transações (hashing) | 🟢 Fácil | 1,5h | `java/m05-algoritmos` · testes: `AuditoriaTransacoesTest` |
| 05.02 | Janelas deslizantes em métricas de negócio | 🟡 Médio | 1,5–2h | `java/m05-algoritmos` · testes: `JanelasTest` |
| 05.03 | Pilhas: validação, monotonicidade e avaliação | 🟡 Médio | 2h | `java/m05-algoritmos` · testes: `PilhasTest` |
| 05.04 | Busca binária (inclusive na resposta) | 🟡 Médio | 2h | `java/m05-algoritmos` · testes: `BuscaBinariaTest` |
| 05.05 | Grafos: rotas de entrega e ordem de build | 🔴 Difícil | 2,5–3h | `java/m05-algoritmos` · testes: `GrafosTest` |
| 05.06 | Agenda de salas de reunião (intervalos) | 🟡 Médio | 1,5–2h | `java/m05-algoritmos` · testes: `AgendaDeSalasTest` |

### 06 · Concorrência

Threads, ExecutorService, CompletableFuture, virtual threads, structured concurrency e como evitar race conditions.

**Objetivos:**

- Explicar a diferença entre threads de plataforma e virtual threads
- Identificar e corrigir race conditions e deadlocks
- Compor chamadas paralelas com CompletableFuture e structured concurrency

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Threads e memória compartilhada | 50 min | 5 |
| 2 | Sincronização, locks e tipos atômicos | 55 min | 5 |
| 3 | Executors e pools de threads | 45 min | 5 |
| 4 | CompletableFuture — compondo chamadas assíncronas | 55 min | 5 |
| 5 | Virtual threads (Project Loom) | 50 min | 5 |
| 6 | Structured concurrency, scoped values e coleções concorrentes | 40 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 06.01 | Contador de acessos thread-safe | 🟢 Fácil | 1h | `java/m06-concorrencia` · testes: `ContadorDeAcessosTest` |
| 06.02 | Reserva de assentos sem overbooking | 🟡 Médio | 1,5–2h | `java/m06-concorrencia` · testes: `SalaDeCinemaTest` |
| 06.03 | Cotação de frete paralela com timeout | 🟡 Médio | 1,5–2h | `java/m06-concorrencia` · testes: `CotadorDeFreteTest` |
| 06.04 | Emissão de notas em lote com virtual threads | 🔴 Difícil | 2h | `java/m06-concorrencia` · testes: `ProcessadorDeNotasTest` |

### 07 · Bancos relacionais e SQL

Modelagem, normalização, PostgreSQL na prática, JOINs, índices com EXPLAIN e transações com níveis de isolamento.

**Objetivos:**

- Modelar um domínio em tabelas normalizadas com chaves e constraints
- Escrever consultas com JOIN, GROUP BY e subconsultas
- Ler um plano de execução e criar o índice certo

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Modelagem relacional e normalização | 50 min | 5 |
| 2 | DDL, tipos e constraints | 45 min | 5 |
| 3 | Consultas e JOINs | 50 min | 5 |
| 4 | Agregações, CTEs e window functions | 60 min | 5 |
| 5 | Índices e EXPLAIN | 50 min | 5 |
| 6 | Transações, isolamento e JDBC | 60 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 07.01 | Banco de uma clínica médica (DDL e constraints) | 🟢 Fácil | 1–1,5h | `java/m07-sql` · testes: `ModelagemClinicaTest` |
| 07.02 | Relatórios de vendas (JOIN, GROUP BY, HAVING) | 🟡 Médio | 1,5–2h | `java/m07-sql` · testes: `RelatoriosVendasTest` |
| 07.03 | Análises com window functions e CTEs | 🔴 Difícil | 2–3h | `java/m07-sql` · testes: `AnalisesAvancadasTest` |
| 07.04 | Repositório de contas com JDBC e transação | 🔴 Difícil | 2–3h | `java/m07-sql` · testes: `RepositorioDeContasTest` |

### 08 · HTTP, REST e Spring Boot 4

HTTP de verdade, APIs REST bem desenhadas e Spring Boot 4: IoC/DI, MVC, Bean Validation, ProblemDetail (RFC 9457), versionamento, OpenAPI e RestClient.

**Objetivos:**

- Desenhar recursos, verbos e status HTTP corretos
- Construir uma API com DTOs validados e erros padronizados
- Explicar IoC/DI e o ciclo de vida de beans

**Projeto integrador:** Fintech Core — Marco 1: API de contas

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | HTTP na prática | 50 min | 5 |
| 2 | Design de APIs REST | 45 min | 5 |
| 3 | IoC, injeção de dependências e beans | 55 min | 5 |
| 4 | Spring Boot 4 e Spring MVC | 60 min | 5 |
| 5 | Validação e erros padronizados | 55 min | 5 |
| 6 | RestClient, versionamento e OpenAPI | 60 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 08.01 | Motor de comissões com injeção de dependências | 🟢 Fácil | 1,5–2h | `java/m08-spring-boot` · testes: `CalculadoraDeComissaoTest,ComissaoConfiguradaTest` |
| 08.02 | API REST de pacientes | 🟡 Médio | 3–4h | `java/m08-spring-boot` · testes: `PacientesApiTest` |
| 08.03 | Erros padronizados com Problem Details | 🟡 Médio | 2–3h | `java/m08-spring-boot` · testes: `ErrosPadronizadosTest` |
| 08.04 | Cliente HTTP de câmbio com cache e fallback | 🔴 Difícil | 3–4h | `java/m08-spring-boot` · testes: `ConversorDeMoedasTest` |
| 08.05 | Fintech Core — Marco 1: API de contas | 🔴 Difícil | 6–10h | `java/projeto-integrador` · testes: `Marco1ContasTest` |

### 09 · Persistência com JPA

Hibernate 7 e Spring Data sem mistério: mapeamentos, N+1, transações, locking, paginação e migrações com Flyway.

**Objetivos:**

- Mapear relacionamentos sem cair no N+1
- Controlar transações e concorrência com locking otimista/pessimista
- Versionar o schema com Flyway

**Projeto integrador:** Fintech Core — Marco 2: transferências atômicas

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | JPA, entidades e o contexto de persistência | 55 min | 5 |
| 2 | Relacionamentos entre entidades | 55 min | 5 |
| 3 | Spring Data JPA na prática | 60 min | 5 |
| 4 | O problema N+1 e o desempenho do JPA | 60 min | 5 |
| 5 | @Transactional a fundo | 55 min | 5 |
| 6 | Locking otimista, pessimista e migrações com Flyway | 60 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 09.01 | Matrículas — entidades, relacionamentos e a última vaga | 🟡 Médio | 3–4h | `java/m09-persistencia` · testes: `MatriculasTest` |
| 09.02 | Painel de pedidos sem N+1 | 🔴 Difícil | 3–4h | `java/m09-persistencia` · testes: `PainelDePedidosTest` |
| 09.03 | Transferências concorrentes — otimista × pessimista | 🔴 Difícil | 3–4h | `java/m09-persistencia` · testes: `TransferenciasTest` |
| 09.04 | Busca de imóveis com filtros dinâmicos | 🟡 Médio | 2–3h | `java/m09-persistencia` · testes: `BuscaDeImoveisTest` |
| 09.05 | Fintech Core — Marco 2: persistência e transferências atômicas | 🔴 Difícil | 8–12h | `java/projeto-integrador` · testes: `Marco1ContasTest,Marco2TransferenciasTest` |

### 10 · Testes automatizados

JUnit 6, AssertJ, Mockito, MockMvcTester, Testcontainers 2 e ArchUnit — e quando usar cada um.

**Objetivos:**

- Escrever testes legíveis, rápidos e determinísticos
- Testar a camada web e a de persistência com ferramentas reais
- Aplicar TDD em uma regra de negócio

**Projeto integrador:** Fintech Core — Marco 3: suíte de testes

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Por que testar — e como escrever um bom teste | 100 min | 7 |
| 2 | JUnit 6 e AssertJ a fundo | 100 min | 7 |
| 3 | Dublês de teste com Mockito | 100 min | 7 |
| 4 | Testes de fatia no Spring Boot | 100 min | 7 |
| 5 | Testes de integração com Testcontainers | 100 min | 7 |
| 6 | TDD e testes de arquitetura | 105 min | 7 |
| 7 | Testes de contrato entre serviços | 110 min | 7 |
| 8 | Testes de desempenho e carga | 120 min | 8 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 10.01 | Caça-mutantes: a suíte de testes da calculadora de frete | 🟡 Médio | 2–3h | `java/m10-testes` · starter + solução |
| 10.02 | Caça-mutantes com Mockito: a suíte de testes do checkout | 🔴 Difícil | 3–4h | `java/m10-testes` · starter + solução |
| 10.03 | Fila de tarefas no PostgreSQL com Testcontainers | 🔴 Difícil | 3–4h | `java/m10-testes` · starter + solução |
| 10.04 | Regras de arquitetura com ArchUnit | 🟡 Médio | 2h | `java/m10-testes` · starter + solução |
| 10.05 | Fintech Core — Marco 3: suíte de testes | 🔴 Difícil | 8–10h | `java/projeto-integrador` · starter + solução |

### 11 · Segurança

Spring Security 7, OAuth2 Resource Server com JWT, senhas com BCrypt, autorização por dono (BOLA), OWASP Top 10:2025 e rate limiting.

**Objetivos:**

- Configurar a SecurityFilterChain e entender o fluxo de filtros
- Proteger uma API com JWT e autorização por papéis
- Reconhecer e prevenir as vulnerabilidades do OWASP Top 10

**Projeto integrador:** Fintech Core — Marco 4: autenticação

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Autenticação e senhas do jeito certo | 120 min | 8 |
| 2 | A arquitetura do Spring Security 7 | 120 min | 8 |
| 3 | JWT e OAuth2 Resource Server | 120 min | 8 |
| 4 | Autorização, RBAC e BOLA | 120 min | 8 |
| 5 | OWASP Top 10:2025 para quem escreve backend Java | 150 min | 8 |
| 6 | Defesas da API: limites, headers, CORS, segredos e dependências | 120 min | 8 |
| 7 | OAuth 2 e OpenID Connect na prática | 130 min | 8 |
| 8 | Modelagem de ameaças e proteção de dados sensíveis | 120 min | 8 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 11.01 | Credenciais: hash, política de senhas, bloqueio e migração | 🟡 Médio | 2–3h | `java/m11-seguranca` · starter + solução |
| 11.02 | API stateless com JWT e papéis | 🔴 Difícil | 3–4h | `java/m11-seguranca` · starter + solução |
| 11.03 | Autorização por dono: a API de RH | 🟡 Médio | 2h | `java/m11-seguranca` · starter + solução |
| 11.04 | Limite de requisições com token bucket | 🟡 Médio | 2–3h | `java/m11-seguranca` · starter + solução |
| 11.05 | Fintech Core — Marco 4: autenticação, JWT e contas com dono | 🔴 Difícil | 8–10h | `java/projeto-integrador` · starter + solução |

### 12 · Mensageria e EDA

RabbitMQ 4 e Kafka 4 (KRaft) na prática: roteamento, partições e chaves, Transactional Outbox, consumidores idempotentes, retry com backoff e DLQ.

**Objetivos:**

- Escolher entre fila e log de eventos
- Garantir entrega confiável com Transactional Outbox
- Construir consumidores idempotentes com DLQ

**Projeto integrador:** Fintech Core — Marco 5: eventos

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Por que mensageria: acoplamento, eventos e garantias de entrega | 100 min | 8 |
| 2 | RabbitMQ na prática com Spring AMQP 4 | 110 min | 8 |
| 3 | Kafka 4: tópicos, partições, chaves e consumer groups | 110 min | 8 |
| 4 | Transactional Outbox: o problema da escrita dupla | 110 min | 8 |
| 5 | Consumidores idempotentes: duplicatas, chaves e efeitos que acontecem uma vez | 110 min | 8 |
| 6 | Falhas no consumo: retry com backoff, DLQ e mensagens envenenadas | 120 min | 8 |
| 7 | Evolução de esquemas de eventos: mudar o evento sem quebrar quem consome | 110 min | 8 |
| 8 | Kafka em produção: partições, durabilidade, lag, rebalance e exactly-once | 130 min | 8 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 12.01 | Roteamento de notificações com RabbitMQ | 🟡 Médio | 2–3h | `java/m12-mensageria` · starter + solução |
| 12.02 | Projeção de estoque com Kafka: chaves, ordem e DLT | 🔴 Difícil | 3–4h | `java/m12-mensageria` · starter + solução |
| 12.03 | O relay do transactional outbox | 🟡 Médio | 2–3h | `java/m12-mensageria` · starter + solução |
| 12.04 | Consumidor idempotente de pagamentos | 🔴 Difícil | 3h | `java/m12-mensageria` · starter + solução |
| 12.05 | Fintech Core — Marco 5: eventos com outbox, Kafka e notificações | 🔴 Difícil | 8–10h | `java/projeto-integrador` · starter + solução |

### 13 · Arquitetura

SOLID sem dogma, padrões que realmente aparecem, arquitetura hexagonal, monólito modular com Spring Modulith 2, microsserviços com critério e resiliência com Spring Framework 7 e Resilience4j.

**Objetivos:**

- Refatorar código legado aplicando SOLID
- Isolar o domínio com portas e adaptadores
- Argumentar monólito modular vs microsserviços

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | SOLID sem dogma | 110 min | 8 |
| 2 | Padrões de projeto que realmente aparecem no backend | 110 min | 8 |
| 3 | Arquitetura hexagonal: portas, adaptadores e a regra da dependência | 110 min | 8 |
| 4 | Monólito modular com Spring Modulith | 110 min | 8 |
| 5 | Microsserviços: quando, por que e a que custo | 120 min | 8 |
| 6 | Resiliência: timeouts, retry, circuit breaker e bulkhead | 110 min | 8 |
| 7 | Domain-Driven Design na prática: do mapa de contextos ao agregado | 110 min | 8 |
| 8 | Decisões e evolução da arquitetura: ADRs, fitness functions e migração sem big bang | 110 min | 8 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 13.01 | Refatorando o frete: Strategy e aberto/fechado, com golden master | 🟡 Médio | 2–3h | `java/m13-arquitetura` · starter + solução |
| 13.02 | Reserva de salas em arquitetura hexagonal | 🔴 Difícil | 4–5h | `java/m13-arquitetura` · starter + solução |
| 13.03 | Da bola de lama ao monólito modular com Spring Modulith | 🔴 Difícil | 3h | `java/m13-arquitetura` · starter + solução |
| 13.04 | Um cliente de câmbio que sobrevive ao provedor | 🔴 Difícil | 3h | `java/m13-arquitetura` · starter + solução |
| 13.05 | Decisão de arquitetura: monólito modular ou microsserviços? (ADR) | 🟡 Médio | 1h30 | — |

### 14 · DevOps e observabilidade

Git no time, Maven/Gradle, Docker multi-stage, Compose, GitHub Actions, OpenTelemetry, logs estruturados e noções de Kubernetes e AWS.

**Objetivos:**

- Empacotar e rodar a aplicação em containers enxutos
- Montar um pipeline de CI que roda testes de integração
- Instrumentar a aplicação com métricas, logs e traces

**Projeto integrador:** Fintech Core — Marco 6: produção

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Git no time: branches curtas, pull requests e versões | 50 min | 6 |
| 2 | Maven e Gradle: ciclo de vida, dependências e o jar executável | 55 min | 6 |
| 3 | Docker: imagens enxutas para Spring Boot e o ambiente local com Compose | 65 min | 6 |
| 4 | CI/CD com GitHub Actions: do pull request à produção | 55 min | 6 |
| 5 | Observabilidade: logs estruturados, métricas, health checks e traces | 75 min | 7 |
| 6 | Kubernetes e cloud: o que um dev backend precisa saber | 55 min | 6 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 14.01 | Logs que o suporte consegue usar | 🟡 Médio | 2h | `java/m14-devops` · testes: `LogsEstruturadosTest` |
| 14.02 | Métricas de negócio para o painel de pagamentos | 🟡 Médio | 2h | `java/m14-devops` · testes: `MetricasTest` |
| 14.03 | Liveness, readiness e o modo de manutenção | 🟡 Médio | 1h30–2h | `java/m14-devops` · testes: `SaudeTest` |
| 14.04 | Da revisão reprovada à entrega pronta — Dockerfile, Compose e CI | 🟡 Médio | 2–3h | `java/m14-devops` · testes: `EntregaTest` |
| 14.05 | Fintech Core — Marco 6: pronto para produção | 🔴 Difícil | 8–10h | `java/projeto-integrador` · testes: `Marco6ProducaoTest` |

### 15 · Cache, NoSQL e System Design

Redis, MongoDB e o framework para responder entrevistas de system design: estimativas, CAP, particionamento, saga e o caso PIX.

**Objetivos:**

- Aplicar cache-aside com invalidação consciente
- Estruturar uma resposta de system design em 45 minutos
- Discutir trade-offs de consistência e disponibilidade

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Cache: onde guardar, como invalidar e como não derrubar o banco | 60 min | 6 |
| 2 | Redis: estruturas, atomicidade e os limites do lock distribuído | 60 min | 5 |
| 3 | MongoDB: modelar documentos, indexar e saber quando não usar | 60 min | 5 |
| 4 | Entrevista de system design: um roteiro para 45 minutos | 55 min | 5 |
| 5 | Escala e consistência: réplicas, partições, CAP e sagas | 65 min | 6 |
| 6 | Estudo de caso: projetando o recebimento de PIX num banco digital | 75 min | 6 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 15.01 | Cache-aside com Redis que aguenta produção | 🟡 Médio | 2h–2h30 | `java/m15-system-design` · testes: `CacheAsideTest` |
| 15.02 | Um catálogo de produtos no MongoDB | 🟡 Médio | 2h30–3h | `java/m15-system-design` · testes: `CatalogoMongoTest` |
| 15.03 | Hash consistente com nós virtuais | 🟡 Médio | 1h30 | `java/m15-system-design` · testes: `AnelDeHashTest` |
| 15.04 | Saga de transferência entre bancos: compensar sem criar dinheiro | 🔴 Difícil | 2h | `java/m15-system-design` · testes: `SagaDeTransferenciaTest` |
| 15.05 | System design: um encurtador de URLs | 🟡 Médio | 1h30–2h | — |

### 16 · IA no backend

Spring AI (chat, RAG, tool calling) e como usar assistentes de código com responsabilidade — e revisar o que eles produzem.

**Objetivos:**

- Integrar um LLM a uma API com Spring AI
- Implementar RAG simples com embeddings
- Revisar criticamente código gerado por IA

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | LLM para devs backend: tokens, contexto, custo e quando não usar | 50 min | 7 |
| 2 | Spring AI 2.0: ChatClient, prompts, saída estruturada e testes sem API | 60 min | 7 |
| 3 | RAG e embeddings: respostas com base nos documentos do banco | 65 min | 7 |
| 4 | Tool calling e MCP: deixar o modelo pedir, e a aplicação decidir | 65 min | 8 |
| 5 | IA em produção: segurança, dados, custo, resiliência e avaliação | 60 min | 7 |
| 6 | Assistentes de código com responsabilidade: onde ajudam, onde erram e como revisar | 50 min | 7 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 16.01 | Classificador de lançamentos com saída estruturada | 🟡 Médio | 1h30 | `java/m16-ia-backend` · testes: `ClassificadorDeLancamentosTest` |
| 16.02 | Busca semântica com trechos, cosseno e isolamento por cliente | 🟡 Médio | 1h30–2h | `java/m16-ia-backend` · testes: `BaseDeConhecimentoTest` |
| 16.03 | Ferramentas bancárias com autorização pelo ToolContext | 🟡 Médio | 1h30 | `java/m16-ia-backend` · testes: `FerramentasBancariasTest` |
| 16.04 | Resumo de chamados sem vazar dados nem obedecer a quem escreveu | 🔴 Difícil | 2h | `java/m16-ia-backend` · testes: `MascaradorDePiiTest,ResumidorDeChamadosTest` |
| 16.05 | Code review: o assistente financeiro gerado por IA | 🟡 Médio | 1h–1h30 | — |

### 17 · Carreira e entrevistas

Portfólio, currículo que passa em ATS, LinkedIn, entrevistas no Brasil e no exterior, inglês técnico, formatos de contratação e negociação.

**Objetivos:**

- Montar um portfólio que prova as habilidades da trilha
- Preparar-se para cada etapa do processo seletivo
- Comparar CLT, PJ e contratos internacionais

**Aulas:**

| # | Aula | Duração | Flashcards |
|---|---|---|---|
| 1 | Portfólio e GitHub: provas em vez de promessas | 45 min | 5 |
| 2 | Currículo, ATS e LinkedIn | 50 min | 5 |
| 3 | O processo seletivo, etapa por etapa | 55 min | 5 |
| 4 | Vagas internacionais e inglês técnico | 55 min | 6 |
| 5 | CLT, PJ e contractor: comparar propostas de verdade | 55 min | 6 |
| 6 | Negociação e os primeiros meses no emprego | 45 min | 5 |

**Exercícios:**

| Código | Exercício | Dificuldade | Tempo | Testes |
|---|---|---|---|---|
| 17.01 | O README do projeto âncora | 🟢 Fácil | 1h–1h30 | — |
| 17.02 | Currículo contra a vaga | 🟡 Médio | 1h30–2h | — |
| 17.03 | Banco de histórias STAR, em português e em inglês | 🟡 Médio | 2h–3h | — |
| 17.04 | Três propostas: CLT, PJ e contractor | 🟡 Médio | 1h30–2h | — |
| 17.05 | Negociação por escrito, em inglês e em português | 🟡 Médio | 1h–1h30 | — |

## Projetos de portfólio

Especificações completas para construir do zero e mostrar em entrevistas (sem código de referência).

| Nível | Projeto | Domínio | Horas | Marcos | Stack |
|---|---|---|---|---|---|
| Júnior | **Agenda Fácil — API de agendamentos para clínicas e consultórios** | Saúde · agendamentos | ~84h | 8 | Java 25, Spring Boot 4, Spring Security 7, Spring Data JPA, PostgreSQL 18, Flyway, JUnit 6, AssertJ, Mockito, Testcontainers 2, Docker Compose, Mailpit, GitHub Actions |
| Júnior | **Estoque Certo — gestão de estoque e compras para pequeno varejo** | Varejo · estoque | ~86h | 9 | Java 25, Spring Boot 4, Spring Security 7, Spring Data JPA, PostgreSQL 18, Flyway, JUnit 6, Testcontainers 2, Docker Compose, GitHub Actions |
| Júnior | **Ozamigos Share — POC de compartilhamento de tela de jogos** | Jogos · tempo real (prova de conceito) | ~33h | 9 | Java 25, Spring Boot 4, WebSocket, Jackson 3, JUnit 6, AssertJ, HTML, JavaScript, WebRTC, Docker, Render |
| Pleno | **Pede Aí — plataforma de pedidos para delivery** | Food delivery · pedidos | ~140h | 9 | Java 25, Spring Boot 4, Spring Modulith, Spring Security 7, Keycloak, PostgreSQL 18, Flyway, Kafka 4, Redis, Resilience4j, OpenTelemetry, Testcontainers 2, k6, Docker Compose, GitHub Actions |
| Pleno | **Ingresso Já — venda de ingressos com alta concorrência** | Eventos · ticketing | ~138h | 9 | Java 25, Spring Boot 4, Spring Security 7, PostgreSQL 18, Flyway, Redis, RabbitMQ, Resilience4j, Micrometer, OpenTelemetry, Testcontainers 2, k6, Docker Compose, GitHub Actions |
| Pleno | **Arquibancada — compartilhamento de tela de jogos em 1080p a 60 fps** | Jogos · transmissão em tempo real | ~132h | 8 | Java 25, Spring Boot 4, Spring Security 7, OAuth2 Login (Google e GitHub), Spring Session, PostgreSQL 18, Flyway, Redis 8, LiveKit (SFU WebRTC), React, TypeScript, Vite, Caddy, Testcontainers 2, k6, OpenTelemetry, Grafana, Docker Compose, GitHub Actions |
| Sênior | **Cobra+ — plataforma SaaS multi-tenant de cobrança recorrente** | Fintech · billing de assinaturas | ~200h | 9 | Java 25, Spring Boot 4, Spring Security 7, Spring Modulith 2, Hibernate 7, Flyway, PostgreSQL 18, Kafka 4 (KRaft), Redis, Resilience4j, Micrometer + OpenTelemetry, Grafana · Prometheus · Tempo · Loki, Keycloak, k6, JUnit 6, Testcontainers 2, Docker Compose, GitHub Actions |
| Sênior | **Rota Viva — despacho e rastreamento de entregas em tempo real** | Logística · last mile | ~204h | 9 | Java 25, Spring Boot 4, Spring Security 7, PostgreSQL 18, PostGIS (opcional), Kafka 4 (KRaft), Redis, Resilience4j, OpenTelemetry, Prometheus, Grafana, Tempo, Loki, Keycloak, Testcontainers 2, k6, Toxiproxy, Docker Compose, Kubernetes (kind), Helm, GitHub Actions |
| Sênior | **Resenha — voz, chat e transmissão de jogos a 60 fps entre amigos** | Comunicação em tempo real · jogos | ~226h | 9 | Java 25, Spring Boot 4, Spring Security 7, Spring Modulith, WebSocket, PostgreSQL 18, Redis 8, Keycloak, LiveKit (SFU WebRTC), SeaweedFS (S3), Electron, TypeScript, Testcontainers 2, k6, OpenTelemetry, Grafana, Docker Compose, Caddy, GitHub Actions |

## Simulador de entrevista

| Tema | Perguntas |
|---|---|
| AI in the backend (EN) | 1 |
| Algorithms (EN) | 1 |
| Architecture (EN) | 1 |
| Arquitetura | 4 |
| Banco de dados | 2 |
| Behavioral (EN) | 11 |
| Carreira | 2 |
| Coleções | 3 |
| Comportamental | 2 |
| Concorrência | 2 |
| Concurrency (EN) | 1 |
| Data structures (EN) | 1 |
| Databases (EN) | 2 |
| DevOps | 4 |
| DevOps (EN) | 2 |
| Engineering practices (EN) | 1 |
| Exceções | 1 |
| Fundamentos | 6 |
| Generics | 1 |
| HTTP | 2 |
| HTTP (EN) | 1 |
| IA no backend | 4 |
| JPA | 2 |
| JPA (EN) | 1 |
| Java (EN) | 2 |
| Java moderno | 2 |
| Mensageria | 5 |
| Messaging (EN) | 1 |
| POO | 5 |
| SQL | 1 |
| Security (EN) | 1 |
| Segurança | 6 |
| Spring | 3 |
| Spring (EN) | 3 |
| System design | 2 |
| System design (EN) | 3 |
| Testes | 2 |
| Testing (EN) | 1 |
