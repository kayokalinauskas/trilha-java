---
pergunta: "O que acontece se um método @Transactional for chamado por outro método da mesma classe?"
tema: "Spring"
nivel: pleno
idioma: pt
modulo: 09-persistencia
intencao: "Entendimento de proxies e AOP no Spring — pergunta de filtro clássica."
pontosChave:
  - "O Spring aplica @Transactional por proxy"
  - "Chamada interna (this.metodo()) não passa pelo proxy"
  - "A anotação é ignorada: sem transação nova, sem rollback esperado"
  - "Soluções: mover para outro bean, TransactionTemplate"
  - "Mesma limitação para @Async e @Cacheable"
armadilha: "Achar que a anotação sempre funciona só por estar no método."
---

"O `@Transactional` funciona por **proxy**: o Spring injeta nos outros beans um objeto intermediário que abre a transação, chama o método real e faz commit ou rollback. Se um método da própria classe chama `this.outroMetodo()`, a chamada acontece direto no objeto, **sem passar pelo proxy**, e a anotação do método interno é ignorada: não abre transação nova, nem aplica o `REQUIRES_NEW` ou as regras de rollback que eu esperava.

As soluções são: mover o método para outro bean (a mais limpa, e costuma indicar uma responsabilidade que merecia estar separada), usar `TransactionTemplate` para controle programático, ou, menos recomendado, injetar a própria classe. A mesma limitação vale para `@Async`, `@Cacheable` e qualquer anotação baseada em proxy. Também vale lembrar que, por padrão, o rollback só acontece para exceções **unchecked**."
