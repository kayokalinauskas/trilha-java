---
pergunta: "Qual a diferença entre == e equals?"
tema: "Fundamentos"
nivel: jr
idioma: pt
modulo: 01-java-essencial
intencao: "Base de qualquer código Java correto."
pontosChave:
  - "== compara valores para primitivos e referências para objetos"
  - "equals compara conteúdo, conforme a implementação da classe"
  - "Constante na frente (\"X\".equals(v)) ou Objects.equals para evitar NPE"
  - "BigDecimal: compareTo"
---

"Para primitivos, `==` compara os valores. Para objetos, compara se as duas variáveis apontam para **o mesmo objeto** na memória. `equals` compara o **conteúdo**, de acordo com a implementação da classe; em `Object`, o padrão é o mesmo que `==`, mas `String`, `Integer`, records e as coleções sobrescrevem.

Então, para comparar Strings, uso `equals`. Se um dos lados pode ser nulo, coloco a constante na frente (`"ADMIN".equals(perfil)`) ou uso `Objects.equals(a, b)`. E um caso especial: `BigDecimal.equals` considera a escala, então `2.0` e `2.00` são diferentes; para valor numérico, uso `compareTo`."
