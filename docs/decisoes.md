# Decisões de arquitetura (ADRs)

Registro curto das decisões que moldam o projeto. Cada uma diz o contexto, a decisão e as consequências.

---

## ADR-001 — Astro + MDX em vez de HTML gerado por scripts

**Contexto.** A v1 era um `index.html` de 263 KB gerado por 8 scripts Python ad-hoc, com o mesmo conteúdo duplicado em Markdown, HTML e JSON. Com ~90 aulas e mais de 110 exercícios previstos, isso seria impossível de manter.

**Decisão.** Usar **Astro** (site estático) com **MDX** e *content collections* tipadas (Zod). Busca offline com **Pagefind**, destaque de código com **Expressive Code** (Shiki) e fontes auto-hospedadas.

**Consequências.**
- ➕ Conteúdo em arquivos pequenos, revisáveis em PR; o build valida o schema e as referências.
- ➕ HTML estático rápido, publicável de graça no GitHub Pages, sem framework de UI pesado no cliente.
- ➖ Exige Node.js para build (não abre mais com duplo clique no arquivo). Mitigado com o deploy automático e o PWA para uso offline.

## ADR-002 — Exercícios como projeto Maven com testes compartilhados

**Contexto.** A proposta exige soluções robustas. Soluções só em texto tendem a ficar erradas ou desatualizadas.

**Decisão.** Cada módulo tem `testes/` (compartilhado), `starter/` e `solucao/`. O `build-helper-maven-plugin` adiciona `../testes` como fonte de teste nos dois. O CI roda `mvn verify` nas soluções e `test-compile` nos starters.

**Consequências.**
- ➕ A solução mostrada no site é garantidamente correta: o componente `CodigoJava` lê o mesmo arquivo que o CI testa.
- ➕ O estudante trabalha como numa empresa: guiado por testes, com Maven e IDE.
- ➖ Os testes fixam uma API mínima, o que reduz a liberdade de design. Mitigado deixando para o estudante as decisões internas e os desafios opcionais.

## ADR-003 — Solução trancada, e não oculta por CSS

**Contexto.** O usuário pediu que a solução não aparecesse "na hora, só se pedir".

**Decisão.** A solução fica em `<Solucao>`: conteúdo com `hidden`, fora do índice de busca (`data-pagefind-ignore="all"`), revelado só após um diálogo de confirmação. O painel registra se a solução foi vista e diferencia exercícios "concluídos sem ver a solução".

**Consequências.** Um usuário determinado consegue ler o HTML-fonte; o objetivo é evitar espiadas acidentais e incentivar a autonomia, não impedir o acesso.

## ADR-004 — Progresso só no navegador

**Contexto.** Login e backend aumentariam custo, complexidade e questões de privacidade (LGPD) para um projeto educacional gratuito.

**Decisão.** Todo o progresso fica no `localStorage` (chave `trilha-java:v2`), com exportar/importar JSON no painel.

**Consequências.** ➕ Zero infraestrutura e privacidade total. ➖ Sem sincronização automática entre dispositivos (mitigado pelo backup).

## ADR-005 — Flashcards com SM-2 simplificado

**Decisão.** Algoritmo SM-2 com quatro respostas (errei, difícil, bom, fácil), no máximo 20 cartões novos por sessão, e cartões liberados quando a aula é concluída.

**Consequências.** Simples de explicar e de implementar sem dependências. O FSRS é mais preciso e pode substituí-lo no futuro sem mudar a interface.

## ADR-006 — Java 25 como alvo, compatível com 21 nos primeiros módulos

**Decisão.** `maven.compiler.release` vem da propriedade `java.release`, que vale 25 por padrão. As soluções dos módulos 00–03 evitam recursos posteriores ao 21, e `-Djava.release=21` funciona para quem ainda não atualizou.

**Consequências.** Os recursos do 22+ aparecem nas aulas como notas "Java moderno". Na fase 2, as soluções dos módulos 04–07 também continuaram compatíveis com o 21: Stream Gatherers, structured concurrency e scoped values aparecem nas aulas, mas nenhum teste depende deles. O CI roda com o 25.
