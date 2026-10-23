---
pergunta: "O que é idempotência em HTTP? Qual a diferença entre PUT, PATCH e POST?"
tema: "HTTP"
nivel: jr
idioma: pt
modulo: 08-spring-boot
intencao: "Fundamentos de API REST."
pontosChave:
  - "Idempotente: repetir a requisição tem o mesmo efeito que fazer uma vez"
  - "GET, PUT e DELETE são idempotentes; POST não"
  - "PUT substitui o recurso inteiro; PATCH altera parcialmente"
  - "Idempotency-Key para tornar POST seguro contra retries (pagamentos)"
---

"Uma operação é **idempotente** quando executá-la várias vezes tem o mesmo efeito que executá-la uma vez. Isso importa porque redes falham e clientes fazem *retry*.

`GET`, `PUT` e `DELETE` são idempotentes por definição. `PUT /clientes/42` **substitui** o recurso inteiro pelo corpo enviado: mandar duas vezes dá o mesmo resultado. `PATCH` faz uma alteração **parcial**; pode ou não ser idempotente, dependendo da operação ("defina o e-mail como X" é; "some 1 ao contador" não é). `POST` cria um recurso ou dispara um processamento, e **não** é idempotente: dois POSTs podem criar dois pedidos.

Em APIs de pagamento, isso é crítico. O padrão é o cliente enviar um header `Idempotency-Key` com um UUID; o servidor guarda a chave com o resultado e, se a mesma chave chegar de novo, devolve o resultado anterior em vez de cobrar duas vezes."
