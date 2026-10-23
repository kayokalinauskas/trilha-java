---
pergunta: "Qual a diferença entre int e Integer? Existe alguma armadilha?"
tema: "Fundamentos"
nivel: jr
idioma: pt
modulo: 01-java-essencial
intencao: "Domínio de tipos e das armadilhas de autoboxing que causam bugs reais."
pontosChave:
  - "int é primitivo, não aceita null e é mais eficiente"
  - "Integer é objeto, necessário em coleções e generics"
  - "Comparar Integer com == só funciona na faixa do cache (-128 a 127)"
  - "Unboxing de null lança NullPointerException"
armadilha: "Afirmar que == funciona para Integer porque \"testou e deu certo\" com números pequenos."
---

"`int` é um tipo primitivo: guarda o valor direto, não pode ser `null` e é mais leve. `Integer` é a classe *wrapper*: é um objeto, pode ser `null` e é obrigatório em coleções e generics, como `List<Integer>`. O compilador converte entre os dois automaticamente (*autoboxing* e *unboxing*).

Duas armadilhas: primeiro, comparar `Integer` com `==` compara **referências**. Funciona por acaso entre -128 e 127, porque a JVM mantém um cache desses valores, mas com 128 já dá `false`. O correto é `equals`. Segundo, fazer unboxing de um `Integer` nulo, por exemplo um campo opcional vindo do banco atribuído a um `int`, lança `NullPointerException`."
