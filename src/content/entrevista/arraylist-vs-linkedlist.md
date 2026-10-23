---
pergunta: "Qual a diferença entre ArrayList e LinkedList? Qual você usa?"
tema: "Coleções"
nivel: jr
idioma: pt
modulo: 03-colecoes-generics-excecoes
intencao: "Noção de complexidade e de como as estruturas funcionam na prática."
pontosChave:
  - "ArrayList: array interno, get(i) O(1), add no fim O(1) amortizado"
  - "LinkedList: nós encadeados, get(i) O(n)"
  - "Na prática ArrayList vence quase sempre (localidade de cache, menos memória)"
  - "Para filas, ArrayDeque"
armadilha: "Dizer que LinkedList é melhor para inserir no meio sem considerar o custo de chegar até o meio."
---

"O `ArrayList` usa um array interno: acesso por índice é O(1) e adicionar no fim é O(1) amortizado, porque de vez em quando ele cresce copiando para um array maior. Inserir ou remover no início ou no meio é O(n), porque desloca elementos.

A `LinkedList` é uma lista duplamente encadeada. Na teoria, insere e remove em O(1), mas só se você **já estiver no nó**; para chegar ao meio, percorre a lista em O(n). Cada nó é um objeto separado, o que usa mais memória e aproveita mal o cache da CPU.

Na prática, uso `ArrayList` em praticamente todos os casos. Para fila ou pilha, uso `ArrayDeque`. Nunca precisei de `LinkedList` em código de produção."
