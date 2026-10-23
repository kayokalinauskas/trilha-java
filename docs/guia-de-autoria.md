# Guia de autoria

Como escrever aulas, exercícios e perguntas de entrevista para a trilha — e manter o padrão de qualidade.

## 1. Onde fica cada coisa

| Conteúdo | Arquivo | Validação |
|---|---|---|
| Módulo | `src/content/modulos.json` | schema `modulos` em `src/content.config.ts` |
| Aula | `src/content/aulas/<modulo-id>/<nn>-<slug>.mdx` | schema `aulas` |
| Exercício | `src/content/exercicios/<modulo-id>/<nn>-<slug>.mdx` | schema `exercicios` |
| Código do exercício | `java/mNN-<nome>/{testes,starter,solucao}` | `mvn verify` no CI |
| Pergunta de entrevista | `src/content/entrevista/<slug>.md` | schema `entrevista` |
| Projeto de portfólio | `src/content/projetos/<junior\|pleno\|senior>/<nn>-<slug>.mdx` | schema `projetos` |

O build **falha** se faltar um campo obrigatório ou se uma referência (módulo, aula) não existir.

> ⚠️ **YAML:** valores com `: ` (dois-pontos seguido de espaço) precisam estar entre aspas.
> ⚠️ **MDX:** fora de blocos de código, `<` e `{` são interpretados como JSX. Escreva `List<Integer>` sempre entre crases.

## 2. Estrutura de uma aula

```mdx
---
titulo: Título que diz o que se aprende
descricao: Uma ou duas frases (aparece no topo, na busca e no módulo).
modulo: 03-colecoes-generics-excecoes
ordem: 4
duracao: 55            # minutos (leitura + prática)
objetivos:
  - Verbo no infinitivo + resultado observável
tags: [generics, PECS]
flashcards:
  - frente: Pergunta curta
    verso: Resposta curta e completa.
---
```

Roteiro do corpo (nessa ordem):

1. **Gancho**: por que isso importa no backend (1–2 parágrafos, sem título).
2. **Intuição**: analogia ou problema concreto antes da sintaxe.
3. **Conceito com código**: exemplos pequenos, que compilam e têm nomes de domínio (pedido, conta, frete), nunca `foo`/`bar`.
4. **Diagrama** (`<Figura>`) quando houver estrutura, fluxo ou memória.
5. **Armadilhas** (`<Callout tipo="erro">`) e comparações (`<Comparar>`).
6. **Como cai na entrevista** (`<Callout tipo="entrevista">`).
7. **Quiz** de 3 a 5 questões, cada uma com explicação.
8. **Resumo** em bullets.

## 3. Componentes disponíveis em MDX (sem import)

| Componente | Uso |
|---|---|
| `<Callout tipo="dica\|atencao\|erro\|entrevista\|novidade\|info" titulo="...">` | Caixas de destaque |
| `<Comparar antes="Evite" depois="Prefira">` + 2 blocos | Lado a lado (vira coluna no celular) |
| `<Tabs grupo="so"><Tab rotulo="Windows">…</Tab></Tabs>` | Abas; mesmo `grupo` sincroniza a escolha na página toda |
| `<Figura legenda="...">` + `<svg>` | Diagramas. Use as classes `d-box`, `d-accent`, `d-ok`, `d-info`, `d-violet`, `d-line`, `d-text`, `d-label`, `d-mono`, `d-title`, `d-arrow` para funcionar nos dois temas. Dê um `id` único ao `<marker>` |
| `<Quiz id="unico" questoes={[{ pergunta, opcoes, correta, explicacao }]} />` | `correta` é o índice (base 0). Crases viram `<code>` |
| `<Dicas><Dica nivel={1}>…</Dica></Dicas>` | Dicas progressivas (1 = direção, 2 = caminho, 3 = quase lá) |
| `<Solucao>…</Solucao>` | Solução trancada, fora do índice de busca |
| `<CodigoJava caminho="m02-poo/solucao/src/main/java/..." destacar="12-18" semImports />` | Mostra um arquivo real do projeto Maven |
| `<Marco id="fundacao" numero={1} titulo="..." horas={8}>…</Marco>` | Um marco do plano de execução de um projeto; o `id` precisa estar em `marcos` no frontmatter (vira a âncora `#marco-<id>` e o progresso) |
| `<RodarTestes modulo="m11-seguranca" arquivos={[...]} />` | Quadro "Seu ambiente de prática"; inserido automaticamente pela página do exercício a partir do frontmatter `java` (com `teste`, mostra o comando dos testes do formato antigo) |

## 4. Estrutura de um exercício

Formato atual (módulos 10 em diante, e o 01.04 como exemplo do módulo 01): **o enunciado é a especificação e o que importa**. Não há testes para a pessoa rodar nem programa pronto: o starter traz só o que não é o foco do exercício e um comentário `// Implemente aqui o exercício` onde ela trabalha. Ela experimenta como preferir (um `main` seu, um teste seu, a aplicação rodando) e compara com os exemplos do enunciado. Referências: `01.04` (gerador de slug) e `11.01` (senhas).

Os módulos 00–09 ainda usam o formato antigo (starter com stubs e testes em `java/<modulo>/testes`), que continua funcionando com `java.teste`.

Frontmatter:

```yaml
titulo: Nome do exercício
codigo: "11.01"          # MM.NN, entre aspas
resumo: Uma frase com o desafio.
modulo: 11-seguranca
ordem: 1
dificuldade: medio        # facil | medio | dificil
tempo: 2h
conceitos: [BCrypt, Clock]
aulas: [11-seguranca/01-autenticacao-e-senhas]   # as aulas citadas em "O que você vai praticar"
java:
  modulo: m11-seguranca
  arquivos:                # onde implementar, relativo a java/<modulo>/starter
    - src/main/java/dev/trilhajava/m11/senhas/ServicoDeCredenciais.java
```

O quadro do topo (`RodarTestes`) mostra onde está o starter, como abri-lo no IntelliJ (File → Open → `pom.xml` → Open as Project) e a lista de `arquivos`. Exercício sem código Java (carreira, system design no papel): omita `java`.

Seções do corpo, nesta ordem:

1. **Contexto** — a demanda de negócio realista, com o problema que ela resolve.
2. **O que você vai praticar** — um item por conceito, com 2 a 4 linhas explicando o essencial (o suficiente para fazer o exercício) e um link para a aula. Links internos são **relativos** (a página fica em `/exercicios/<modulo>/<slug>/`): `[Montando texto](../../../aulas/01-java-essencial/05-arrays-e-strings/#montando-texto)`. Âncoras seguem o github-slugger (minúsculas, acentos mantidos, crases e pontuação removidas).
3. **Requisitos** — "O que já existe" (o que o starter traz) e "O que você implementa": assinaturas exatas em bloco de código, o que recebe, o que devolve, o que lança (e se a mensagem é livre), e uma tabela entrada → saída.
4. **Regras** — numeradas, **cada uma com exemplo concreto**, incluindo os casos de borda. Uma regra que não tem exemplo não está bem definida.
5. **Passo a passo sugerido** — etapas pequenas, uma ou duas regras por vez, cada uma dizendo o que experimentar e o que se deve ver. O primeiro passo é montar um jeito de experimentar.
6. **Experimente** — como exercitar o código (um `main` curto de exemplo, `curl`, um teste da própria pessoa…) e os **resultados esperados** numa tabela cenário → resultado, conferidos contra a solução de referência. Diga o que pode variar (mensagens de exceção, hashes, tempos).
7. **Critérios de aceite** — comportamento observável (os cenários dão o resultado indicado, saber explicar algo). **Nunca** estilo de implementação ("percorrer só uma vez"): isso vai para desafios opcionais ou "avalie sua solução".
8. **Desafios opcionais**.
9. `<Dicas>` com 3 níveis.
10. `<Solucao>` com: código (via `CodigoJava`), por que assim, alternativas e trade-offs, "avalie sua solução".

**Sem regex obrigatória.** Nenhum enunciado ou solução de referência depende de expressão regular. Validar formato se faz percorrendo caracteres (`charAt`, `indexOf`, `Character.isDigit`...); remover acentos, com `Normalizer.Form.NFD` + `Character.getType(c) == Character.NON_SPACING_MARK`. Regex pode aparecer só na seção de alternativas da solução, com a expressão explicada peça por peça.

## 5. O código Java de um exercício

```text
java/m11-seguranca/
  starter/src/main/java/.../Usuario.java                  ← apoio pronto (records, interfaces, exceções, fakes em memória)
  starter/src/main/java/.../ServicoDeCredenciais.java     ← a classe do exercício: "// Implemente aqui o exercício"
  solucao/src/main/java/.../ServicoDeCredenciais.java     ← solução de referência (aparece no site via CodigoJava)
  solucao/src/test/java/.../ServicoDeCredenciaisTest.java ← confere a SOLUÇÃO contra o enunciado; não vai para o starter
```

**O starter**

- Traz pronto só o que **não** é o foco: tipos de apoio, interfaces, repositórios em memória, configuração do Spring, migrations. Os mesmos arquivos, idênticos, existem na solução.
- A classe do exercício tem o Javadoc curto ("Os requisitos e as regras estão no enunciado, no site.") e o corpo `// Implemente aqui o exercício`. Sem stubs que lançam exceção, sem `TODO` espalhados, sem programa `Experimente…`.
- Exercício em que a pessoa escreve **testes** (módulo 10): o código a testar vem pronto em `src/main`, e a classe de teste em `src/test/java` tem o comentário.
- O starter **compila** (`mvn -P starters test-compile`).

**A solução**

- Passa no próprio teste em `solucao/src/test/java`, que usa os exemplos do enunciado. Esse teste existe para garantir que o que o site mostra funciona; ele não é apresentado à pessoa.
- No exemplo `main` do enunciado, use `public static void main(String[] args)`, não o `void main()` do Java 25 (roda também com `-Djava.release=21`).
- Para gerar os resultados esperados do enunciado, rode o exemplo contra a solução e confira cada linha da tabela.

**Formato antigo (módulos 00–09)**: stubs que lançam `UnsupportedOperationException` no starter, testes compartilhados em `java/<modulo>/testes` (incluídos pelo `build-helper`, execução `testes-compartilhados`) e `java.teste` no frontmatter. Com `@Nested`, o comando precisa do `*`: `mvn test "-Dtest=DinheiroTest*"` (o `RodarTestes` já gera assim).

Módulo novo: crie os dois `pom.xml` (copie de um módulo existente) e registre-os no `java/pom.xml` (`<modules>` e perfil `starters`).

Verificação local (com o JDK 21, acrescente `-Djava.release=21` aos comandos Maven):

```bash
cd java
mvn verify                              # soluções (e os testes delas)
mvn -P starters test-compile            # starters compilam
```

## 6. Projetos de portfólio

Projetos completos, dois por nível (Júnior, Pleno, Sênior) mais uma trilha de tempo real: Ozamigos Share (Júnior, POC guiada com código), Arquibancada (Pleno) e Resenha (Sênior), para a pessoa construir **do zero, no próprio GitHub**, e mostrar a recrutadores e gestores. O site traz só a especificação: não há starter nem solução de referência (o portfólio precisa ser autoral). O objetivo é treinar para a vaga, então a especificação é tão detalhada quanto a de um time profissional: nada de "faça um CRUD de X".

Página: `/projetos/<nivel>/<nn>-<slug>/`. Links para aulas são relativos: `[Transações](../../../aulas/07-sql/05-transacoes/)`.

Frontmatter:

```yaml
titulo: Agenda Fácil — API de agendamentos para clínicas
resumo: Uma frase: o que o sistema faz, para quem e o que tem de difícil.
nivel: junior              # junior | pleno | senior
ordem: 1
dominio: Saúde · agendamentos
horas: 80                  # soma aproximada das horas dos marcos
stack: [Java 25, Spring Boot 4, PostgreSQL, Flyway, Testcontainers, Docker, GitHub Actions]
competencias:              # 5 a 7 frases: o que quem contrata enxerga no repositório pronto
  - Regras de negócio não triviais (conflito de horários) cobertas por testes
modulos: [08-spring-boot, 09-persistencia, 10-testes]   # módulos que o projeto exercita
marcos:                    # 6 a 9, na ordem; o id vira "#marco-<id>" e a chave do progresso
  - { id: fundacao, titulo: Fundação do repositório, resumo: Projeto criado, CI verde e banco subindo com Docker Compose. }
```

Seções do corpo (`##`), nesta ordem:

1. **Visão geral** — o problema, o que será construído (em 5–8 bullets), o pitch de uma frase e por que esse projeto chama atenção num portfólio do nível.
2. **Contexto de negócio** — a empresa fictícia, os atores (tabela ator → o que faz no sistema) e um glossário do domínio.
3. **Escopo** — "Dentro" e "Fora" explícitos. O "fora" evita que o projeto cresça sem fim e vira extensão em "Para ir além".
4. **Requisitos funcionais** — um `###` por requisito (`### RF-01 · Cadastro de profissionais`), com descrição, **regras numeradas** (`RN-01`…) cada uma com exemplo concreto e caso de borda, e **critérios de aceite** no formato Dado / Quando / Então. Numeração contínua no projeto inteiro.
5. **Requisitos não funcionais** — tabela `RNF-xx | requisito | meta mensurável | como verificar` (desempenho com números, segurança, observabilidade, qualidade, operação).
6. **Modelo de dados** — entidades e relacionamentos num `<Figura>` (diagrama ER em SVG com as classes `d-*`), as restrições importantes (únicas, checks, índices) e o que cada migration cria.
7. **Contrato da API** — tabela de endpoints (método, caminho, quem pode, o que faz, respostas), exemplos completos de requisição e resposta em JSON para os endpoints principais, o formato de erro (Problem Details, RFC 9457) com a tabela de códigos de erro. Pleno e Sênior: catálogo de eventos (nome, produtor, consumidores, chave, payload, garantia de entrega).
8. **Arquitetura** — diagrama de containers/componentes (`<Figura>`), organização de pacotes ou serviços, e as **decisões que a pessoa precisa tomar e registrar em ADRs**: para cada uma, a pergunta, as opções com trade-offs e a recomendação de base.
9. **Plano de execução** — um parágrafo explicando começo, meio e fim, e um `<Marco>` por item de `marcos` (mesmos ids, mesma ordem). Cada marco tem: **Objetivo**, **Tarefas** (lista), **Entregáveis**, **Definição de pronto** (verificável: "o `docker compose up` sobe…", "o teste X passa…") e **Aulas de apoio** (links). O primeiro marco é a fundação (repositório, Spring Initializr, Docker Compose, CI, README inicial); o último é a entrega (deploy, README final, demonstração, release `v1.0.0`). Armadilhas vão em `<Callout tipo="erro">` dentro do marco.
10. **Estratégia de testes** — o que testar em cada nível (unidade, fatia, integração com Testcontainers, contrato, carga quando fizer sentido) e a lista de cenários obrigatórios.
11. **Entregáveis do portfólio** — o modelo de README (em bloco de código), os diagramas, o arquivo `.http`/coleção, o roteiro da demonstração em vídeo de até 3 minutos, badges, deploy e o post para o LinkedIn.
12. **Rubrica de autoavaliação** — tabela `critério | abaixo do nível | no nível | acima do nível`, com 8 a 12 critérios concretos.
13. **Como apresentar na entrevista** — pitch de 60–90 s, 8 a 12 perguntas que o entrevistador provavelmente fará sobre o projeto, com os pontos-chave de uma boa resposta, e os trade-offs que vale mencionar sem esperar a pergunta.
14. **Para ir além** — extensões para quem terminou.
15. **O que estudar além da trilha** — só nos projetos Sênior: cada tópico que a trilha não cobre, com uma explicação curta (o suficiente para começar) e referências oficiais.

Regras de qualidade:

- **Todo requisito é verificável.** Se não dá para escrever o critério de aceite, o requisito está vago.
- **Números reais:** limites, prazos, metas de latência, tamanhos de página, códigos HTTP. Nada de "rápido" ou "seguro".
- **Versões atuais** (Java 25, Spring Boot 4, Spring Security 7, Testcontainers 2, Kafka 4) e APIs conferidas; nenhuma dependência inventada. Serviços externos (pagamento, e-mail, mapas) são **simulados** por um serviço fake que a pessoa também escreve ou sobe via Docker, com o contrato descrito.
- Sem custo obrigatório: tudo roda localmente com Docker; o deploy sugere opções com plano gratuito ou de baixo custo, sem prometer preços.

## 7. Perguntas de entrevista

```yaml
---
pergunta: "Texto da pergunta"
tema: "Coleções"
nivel: jr               # jr | pleno
idioma: pt              # pt | en
modulo: 03-colecoes-generics-excecoes
intencao: "O que o entrevistador quer avaliar."
pontosChave:
  - "Ponto que uma boa resposta precisa cobrir"
armadilha: "Erro comum (opcional)."
---

Resposta-modelo em primeira pessoa, como o candidato falaria (150–300 palavras).
```

## 8. Checklist de qualidade

- [ ] Versões e APIs conferidas com a documentação oficial (veja `docs/curadoria-2026.md`).
- [ ] Todo código de exemplo compila (teste no JShell ou no projeto).
- [ ] Nenhum `double` para dinheiro, nenhum `==` para objetos, nenhum `catch` vazio — nem em exemplos "rápidos".
- [ ] Linguagem neutra e inclusiva; "você" em vez de "o aluno".
- [ ] `npm run build` sem erros e `npm run docs:catalogo` executado.
- [ ] Conferido **primeiro em 1920 × 1080 e 2560 × 1440** (as telas prioritárias: coluna de leitura, código, diagramas e tabelas aproveitando bem o espaço), depois no celular (375 px), sempre nos temas claro e escuro.
