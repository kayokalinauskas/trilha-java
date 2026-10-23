---
pergunta: "Como funciona um HashMap por dentro?"
tema: "Coleções"
nivel: pleno
idioma: pt
modulo: 03-colecoes-generics-excecoes
intencao: "Pergunta de filtro para Pleno: estrutura de dados, contrato equals/hashCode e desempenho."
pontosChave:
  - "hashCode define o bucket; equals resolve colisões"
  - "O(1) em média"
  - "Load factor 0,75 e rehash (dobra a capacidade)"
  - "Desde o Java 8, buckets com muitas colisões viram árvores (pior caso O(log n))"
  - "Chaves mutáveis e hashCode ruim quebram o mapa"
---

"Um `HashMap` é um array de *buckets*. Ao fazer `put`, ele calcula o `hashCode` da chave, espalha os bits e usa o resultado para escolher o bucket. Se já houver chaves ali (colisão), ele percorre o bucket comparando com `equals` para decidir se substitui o valor ou adiciona uma nova entrada. O `get` faz o mesmo caminho. Por isso é O(1) em média.

Quando o número de entradas passa de 75% da capacidade (o *load factor*), ele dobra o array e redistribui tudo (*rehash*). Desde o Java 8, um bucket com muitas colisões (a partir de 8 entradas, com capacidade suficiente) vira uma árvore rubro-negra, então o pior caso cai de O(n) para O(log n).

Consequências práticas: se eu sobrescrevo `equals` sem `hashCode`, objetos iguais caem em buckets diferentes e o mapa aceita duplicados. Se a chave é mutável e o hash muda depois de inserida, ela fica perdida. E o `HashMap` não é thread-safe; para concorrência, `ConcurrentHashMap`."
