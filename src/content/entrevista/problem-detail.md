---
pergunta: "Como você padroniza respostas de erro em uma API Spring Boot?"
tema: "Spring"
nivel: jr
idioma: pt
modulo: 08-spring-boot
intencao: "Conhecimento de boas práticas REST e dos recursos atuais do Spring."
pontosChave:
  - "ProblemDetail implementa a RFC 9457 (que substituiu a 7807)"
  - "@RestControllerAdvice centraliza o tratamento"
  - "Status HTTP correto por tipo de erro"
  - "Campos type, title, status, detail, instance + propriedades extras"
  - "Não vazar stack trace nem detalhes internos"
armadilha: "Devolver 200 com { \"erro\": true } no corpo, ou 500 para erros de validação."
---

"Uso o padrão **Problem Details**, definido pela **RFC 9457** — que substituiu a antiga RFC 7807. O Spring tem a classe `ProblemDetail` nativamente. Crio um `@RestControllerAdvice` com `@ExceptionHandler` para cada categoria de erro e devolvo um `ProblemDetail` com o status HTTP correto: 400 para validação, 404 para recurso inexistente, 409 para conflito de estado, 422 para regra de negócio violada.

O corpo tem `type` (uma URI que identifica o tipo de erro), `title`, `status`, `detail` e `instance`, e eu adiciono propriedades como o código de erro de negócio e a lista de campos inválidos do Bean Validation. Com `spring.mvc.problemdetails.enabled=true`, os erros do próprio framework também seguem o padrão. E nunca devolvo stack trace nem mensagens internas de banco para o cliente: isso vai para o log, com um *correlation id* que aparece na resposta."
