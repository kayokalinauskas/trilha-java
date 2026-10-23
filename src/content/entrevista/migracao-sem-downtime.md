---
pergunta: "Como fazer uma migração de banco em produção sem downtime?"
tema: "Banco de dados"
nivel: pleno
idioma: pt
modulo: 09-persistencia
intencao: "Experiência com deploys contínuos e mudanças de schema seguras."
pontosChave:
  - "Expand and contract em várias versões"
  - "Nunca renomear/remover coluna em uso de uma vez"
  - "Novas colunas nullable ou com default"
  - "Backfill em lotes"
  - "Flyway/Liquibase versionando cada passo; cuidado com locks em tabelas grandes"
---

"Uso o padrão **expand and contract**, em várias versões, porque durante o deploy convivem instâncias com o código antigo e o novo.

Para renomear `nome` para `nome_completo`, por exemplo: **1) expandir**: uma migração do Flyway adiciona `nome_completo` como nullable, e o código novo passa a escrever nas duas colunas. **2) migrar dados**: um backfill copia os dados antigos, **em lotes**, para não travar a tabela. **3) trocar a leitura**: o código passa a ler a coluna nova. **4) contrair**: numa versão seguinte, quando nenhuma instância usa mais a coluna antiga, ela é removida.

Outros cuidados: em tabelas grandes, operações como adicionar uma coluna `NOT NULL` sem default ou criar índice podem bloquear escrita; no PostgreSQL, uso `CREATE INDEX CONCURRENTLY`. E toda migração é versionada e testada no CI, com Testcontainers, antes de chegar a produção."
