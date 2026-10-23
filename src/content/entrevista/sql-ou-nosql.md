---
pergunta: "SQL ou NoSQL: como você decide qual banco usar num projeto novo?"
tema: "Banco de dados"
nivel: pleno
idioma: pt
modulo: 15-system-design
intencao: "Ver se a decisão parte dos padrões de acesso, da consistência e do custo operacional, e não de preferência ou de moda."
pontosChave:
  - "Começar pelos padrões de acesso e pelas garantias de consistência, não pelo produto"
  - "Relacional: entidades relacionadas, transações entre várias linhas (dinheiro, estoque), consultas ad hoc"
  - "Documento: dados autocontidos lidos juntos, atributos variáveis; atomicidade por documento"
  - "NoSQL é uma família: documento, chave-valor, colunar largo, grafo, cada um para um acesso"
  - "Meio-termo (JSONB no PostgreSQL) e o custo de operar mais um banco"
armadilha: "\"NoSQL escala e SQL não\", ou \"NoSQL não tem transação\": os dois são mitos desatualizados."
---

"Eu começo pelos **padrões de acesso** e pela **consistência** que o negócio exige, não pelo nome do banco. Como os dados vão ser lidos e escritos? Quais operações precisam ser atômicas juntas?

Se o domínio tem **entidades relacionadas** e operações que mexem em várias delas ao mesmo tempo, como débito e crédito numa transferência, ou pedido, itens e estoque, eu vou de **relacional**: transações ACID entre linhas e tabelas, constraints que o banco garante, e SQL para as consultas que ninguém previu, como os relatórios do financeiro.

Se os dados são **autocontidos**, lidos sempre juntos e com **forma variável**, como um catálogo em que a camiseta tem cor e tamanho e o notebook tem memória e tela, um banco de **documentos** como o MongoDB encaixa bem: um documento por produto, sem tabela de atributos, e a escrita num documento já é atômica. Um `$inc` com filtro de estoque resolve a reserva sem transação.

Também lembro que 'NoSQL' é uma família: **chave-valor** (Redis, DynamoDB) para acesso por chave com latência baixa; **colunar largo** (Cassandra) para muita escrita, sempre consultada pela mesma chave de partição; **grafo** para relações de muitos saltos. Cada um é ótimo num tipo de acesso e ruim fora dele.

E eu pesaria o custo operacional. Se o sistema já roda PostgreSQL e só uma parte precisa de atributos flexíveis, uma coluna **JSONB** com índice GIN resolve sem mais um banco para fazer backup, monitorar e atualizar. Os mitos eu evito: o MongoDB tem transações multidocumento desde a 4.0, e o PostgreSQL escala muito com réplicas e particionamento. A pergunta certa é qual modelo casa com o acesso."
