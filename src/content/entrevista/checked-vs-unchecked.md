---
pergunta: "Qual a diferença entre exceções checked e unchecked? Quando usar cada uma?"
tema: "Exceções"
nivel: jr
idioma: pt
modulo: 03-colecoes-generics-excecoes
intencao: "Critério de design de erros, não só a definição."
pontosChave:
  - "Checked obriga tratar ou declarar; unchecked não"
  - "Checked para falhas esperadas e recuperáveis"
  - "Unchecked para erros de programação e regras tratadas globalmente"
  - "Em Spring, exceções de negócio unchecked + @RestControllerAdvice"
  - "Traduzir preservando a causa; nunca engolir"
armadilha: "Capturar Exception genérica e \"engolir\" o erro com um catch vazio."
---

"Exceções **checked** herdam de `Exception` e o compilador obriga quem chama a tratar ou declarar com `throws`. **Unchecked** herdam de `RuntimeException` e o tratamento é opcional.

Meu critério: checked para falhas **esperadas e recuperáveis**, em que quem chama tem uma ação clara, como um arquivo de importação inválido, em que dá para avisar o operador e pedir outro. Unchecked para erros de programação, como argumento inválido, e para regras de negócio que são tratadas de forma centralizada.

Em aplicações Spring, crio uma hierarquia de exceções de negócio unchecked, com um código estável tipo `SALDO_INSUFICIENTE`, e um `@RestControllerAdvice` converte em `ProblemDetail` com o status HTTP certo. Assim os métodos não ficam poluídos de `throws`. E quando traduzo uma exceção de baixo nível, sempre passo a original como **causa**, para não perder o stack trace."
