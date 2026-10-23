# ☕ Trilha Java Backend

**Do básico até a vaga de backend Java Júnior e Pleno** — aulas de verdade, exercícios com testes automatizados, soluções comentadas que só aparecem quando você pede, flashcards com repetição espaçada e um simulador de entrevistas (Brasil e internacional).

> Edição 2026 · Java 25 LTS · Spring Boot 4 · JUnit 6
>
> **Acesse online: https://kayokalinauskas.github.io/trilha-java/**

---

## O que tem aqui

| | |
|---|---|
| 🗺️ **Mapa interativo** | 18 módulos com dependências, do primeiro `main` ao system design, com seu progresso em cada um |
| 📚 **Aulas** | Intuição → conceito com código → diagramas → armadilhas → "como cai na entrevista" → quiz |
| 🧪 **Exercícios estilo Exercism** | Cada um tem um projeto Maven com testes JUnit que começam vermelhos. Você implementa até ficarem verdes |
| 💡 **Dicas progressivas** | Três níveis por exercício, antes da solução |
| 🔒 **Solução trancada** | Aparece só com confirmação; fica registrado no painel se você viu. O código exibido é o mesmo que passa nos testes do CI |
| 🃏 **Flashcards (SM-2)** | Os cartões de uma aula entram na fila quando você a conclui |
| 🎤 **Simulador de entrevista** | 95 perguntas (61 em PT, 34 em EN), com a intenção do entrevistador, pontos-chave, armadilha e resposta-modelo. Simulado rápido ou "loop completo" no formato das vagas internacionais (recrutador, técnica, system design, hiring manager), com timer, modo escuta e frases úteis em inglês |
| 📊 **Painel** | Prontidão Júnior/Pleno ponderada por horas, sequência de estudo, heatmap de atividade, próximos passos e backup |
| 🔍 **Busca Ctrl+K** | Pelo título (sempre) e pelo texto completo das aulas (no build, com Pagefind) |
| 🌗 **Tema claro/escuro, responsivo, offline (PWA)** | Sem login e sem servidor: o progresso fica no seu navegador |

### Estado atual (fase 5 — trilha completa)

- ✅ Plataforma completa
- ✅ Os **18 módulos** publicados, do módulo 00 ao 17: 113 aulas, 82 exercícios com solução (54 com testes para o aluno, 20 com starter e solução em Java sem testes prontos e 8 escritos: ADR, system design, revisão de código gerado por IA e os exercícios de carreira), 674 flashcards
- ✅ 9 projetos de portfólio (3 Júnior, 3 Pleno, 3 Sênior), com especificação completa para construir do zero
- ✅ Projeto integrador **Fintech Core**, marcos 1 a 6: API de contas, transferências atômicas com JPA, suíte de testes com Testcontainers e ArchUnit, autenticação com JWT e contas com dono, eventos com outbox e Kafka, e a entrega em produção (Docker, health checks, métricas e logs estruturados)
- ✅ IA no backend com Spring AI 2 (chat, saída estruturada, RAG, tool calling) testada com modelos falsos, sem chave de API
- ✅ Carreira: portfólio, currículo e ATS, entrevistas no Brasil e fora, contratos (CLT, PJ, contractor) e negociação
- ✅ Simulador com 95 perguntas (34 em inglês) cobrindo todos os temas da trilha

---

## Como estudar

1. Abra o [site](https://kayokalinauskas.github.io/trilha-java/) (ou rode localmente, veja abaixo) e comece pelo **módulo 00**.
2. Para cada aula: leia, rode os exemplos no JShell e faça o quiz.
3. Para cada exercício:

   ```bash
   cd java/m02-poo/starter
   mvn test -Dtest=ContaCorrenteTest
   ```

   Implemente no `starter/` até todos os testes passarem. Use as dicas antes da solução.
4. Revise os flashcards todos os dias (5 minutos bastam) e treine no simulador de entrevista.

---

## Rodando localmente

### Site

Requisitos: **Node.js 22+**.

```bash
npm install
npm run dev        # http://localhost:4321
npm run build      # gera dist/ + índice de busca (Pagefind)
npm run preview    # serve o build
```

### Exercícios Java

Requisitos: **JDK 25** (ou 21, veja abaixo) e **Maven 3.9+**.

```bash
cd java
mvn verify                        # compila e testa todas as SOLUÇÕES de referência
mvn -P starters test-compile      # confere que os starters compilam
```

Ainda no JDK 21? Acrescente `-Djava.release=21` (o CI usa o JDK 25). Os módulos a partir do 10 usam Testcontainers: com Docker ligado, os testes de integração rodam; sem Docker, eles são pulados.

---

## Estrutura

```text
├── src/
│   ├── content/
│   │   ├── modulos.json          ← currículo (18 módulos, dependências, objetivos)
│   │   ├── aulas/<modulo>/*.mdx  ← aulas
│   │   ├── exercicios/<modulo>/*.mdx
│   │   └── entrevista/*.md       ← banco do simulador
│   ├── components/               ← mapa, busca, header… e componentes de conteúdo (mdx/)
│   ├── layouts/                  ← Base e Leitura (3 colunas, modo foco)
│   ├── lib/                      ← progresso (localStorage, SM-2), helpers de conteúdo
│   ├── pages/                    ← rotas do site
│   └── styles/                   ← design system (tokens claro/escuro) e tipografia do conteúdo
├── java/                         ← projeto Maven multi-módulo
│   └── mNN-nome/{testes,starter,solucao}
├── docs/                         ← guia de autoria, curadoria de versões, decisões (ADRs)
├── scripts/gerar-catalogo.mjs    ← gera DOCUMENTACAO.md a partir do conteúdo
└── .github/workflows/ci.yml      ← testa soluções, compila starters, builda o site (e publica no Pages, se ligado)
```

## Documentação

- [DOCUMENTACAO.md](DOCUMENTACAO.md) — catálogo completo (gerado automaticamente: `npm run docs:catalogo`)
- [docs/guia-de-autoria.md](docs/guia-de-autoria.md) — como escrever aulas e exercícios, componentes disponíveis, checklist de qualidade
- [docs/curadoria-2026.md](docs/curadoria-2026.md) — versões adotadas, o que mudou em relação à v1, fontes e política de revisão
- [docs/decisoes.md](docs/decisoes.md) — decisões de arquitetura (ADRs)
- [docs/roadmap-de-entregas.md](docs/roadmap-de-entregas.md) — fases de publicação dos módulos

## Deploy

O site é publicado no **GitHub Pages** pelo CI: cada push na `main` testa as soluções, compila os starters, gera o site e publica em https://kayokalinauskas.github.io/trilha-java/.

Num fork, a publicação fica **desligada** até você ligar:

1. Em *Settings → Pages*, selecione **GitHub Actions** como fonte.
2. Em *Settings → Secrets and variables → Actions → Variables*, crie a variável `PUBLICAR_PAGES` com o valor `true`.

O caminho base (`/nome-do-repo/`) é configurado automaticamente.
