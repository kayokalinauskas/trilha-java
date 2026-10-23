---
pergunta: "Qual o contrato entre equals e hashCode? O que acontece se você quebrá-lo?"
tema: "Coleções"
nivel: jr
idioma: pt
modulo: 03-colecoes-generics-excecoes
intencao: "Bug clássico de produção em sets e mapas."
pontosChave:
  - "Objetos equals devem ter o mesmo hashCode"
  - "O inverso não é obrigatório (colisões)"
  - "Usar os mesmos campos nos dois"
  - "Campos imutáveis"
  - "Records geram os dois corretamente"
---

"A regra é: se `a.equals(b)`, então `a.hashCode() == b.hashCode()`. O contrário não precisa valer; hashes iguais para objetos diferentes são colisões normais.

Se eu sobrescrevo `equals` e esqueço o `hashCode`, dois objetos "iguais" têm hashes diferentes, caem em buckets diferentes de um `HashSet` e o `equals` nunca é consultado. O set aceita duplicados e o `contains` não encontra um objeto igual. É um bug silencioso.

Na prática: uso **os mesmos campos** nos dois métodos, de preferência imutáveis, e normalizo na entrada. Por exemplo, se o código de uma peça não diferencia maiúsculas, eu o converto no construtor, e aí os dois métodos comparam o campo diretamente. Para dados simples, uso records, que geram os dois corretamente."
