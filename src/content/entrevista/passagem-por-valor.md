---
pergunta: "Java passa parâmetros por valor ou por referência?"
tema: "Fundamentos"
nivel: jr
idioma: pt
modulo: 01-java-essencial
intencao: "Se você entende o modelo de memória de referências, que explica muitos bugs."
pontosChave:
  - "Sempre por valor"
  - "Para objetos, o valor copiado é a referência"
  - "Alterar o objeto (list.add) é visível fora; reatribuir o parâmetro não é"
armadilha: "Responder \"primitivos por valor, objetos por referência\" — soa certo, mas está tecnicamente errado."
---

"Java **sempre** passa por valor. A confusão é que, para objetos, o valor que é copiado é a **referência**.

Então, se eu recebo uma `List` e faço `lista.add(x)`, estou alterando o mesmo objeto que quem chamou enxerga, e a mudança aparece lá fora. Mas se eu faço `lista = new ArrayList<>()` dentro do método, só a minha cópia da referência passa a apontar para outro objeto; a variável de quem chamou continua apontando para a lista original. Em uma linguagem com passagem por referência de verdade, essa reatribuição seria visível fora.

Na prática, isso é o motivo de eu usar cópias defensivas e objetos imutáveis: um método pode, sem querer, alterar o estado de um objeto que recebeu."
