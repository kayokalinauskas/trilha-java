---
pergunta: "Como usar Optional corretamente? Quais os anti-padrões?"
tema: "Java moderno"
nivel: jr
idioma: pt
modulo: 04-java-moderno
intencao: "Uso idiomático de Optional, que é muito cobrado em code review."
pontosChave:
  - "Optional é para tipo de retorno que pode não ter valor"
  - "Evitar get() sem checagem; preferir orElse, orElseThrow, map"
  - "Não usar como atributo, parâmetro ou em coleções"
  - "orElse avalia sempre; orElseGet é preguiçoso"
armadilha: "Fazer if (opt.isPresent()) { opt.get() } — é só um null check disfarçado."
---

"`Optional` foi pensado como **tipo de retorno** de métodos que podem não ter resultado, como `repositorio.findById(id)`. Ele obriga quem chama a pensar no caso vazio.

O uso idiomático é encadear: `buscarCliente(id).map(Cliente::email).orElseThrow(() -> new ClienteNaoEncontradoException(id))`. Os anti-padrões que evito: chamar `get()` sem checar, ou fazer `isPresent()` seguido de `get()`, que é só um `null` check disfarçado; usar `Optional` como atributo de entidade, parâmetro de método ou elemento de coleção (para coleções, a resposta vazia é uma coleção vazia); e usar `orElse(chamadaCara())`, que executa a chamada mesmo quando há valor — nesse caso, uso `orElseGet`."
