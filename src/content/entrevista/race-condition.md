---
pergunta: "O que é uma race condition? Como evitar?"
tema: "Concorrência"
nivel: pleno
idioma: pt
modulo: 06-concorrencia
intencao: "Fundamentos de concorrência que causam bugs graves em produção."
pontosChave:
  - "Resultado depende da ordem de execução de threads"
  - "count++ não é atômico (ler, somar, escrever)"
  - "synchronized/Lock, tipos atômicos, estruturas concorrentes"
  - "Preferir imutabilidade e não compartilhar estado"
  - "No banco: locks e transações"
---

"Race condition é quando o resultado depende da **ordem** em que threads executam operações sobre um estado compartilhado. O exemplo clássico é `contador++`: parece uma operação, mas são três — ler, somar e escrever. Duas threads podem ler o mesmo valor e uma das atualizações se perde.

As defesas, da melhor para a pior: **não compartilhar estado mutável** (objetos imutáveis, variáveis locais); usar **estruturas feitas para concorrência**, como `AtomicInteger`/`LongAdder` e `ConcurrentHashMap` com operações atômicas tipo `merge`; e, quando preciso de uma seção crítica maior, `synchronized` ou `ReentrantLock`, mantendo o trecho protegido o menor possível.

Em backend, a race condition mais comum nem é em memória: é no **banco**, com duas requisições debitando o mesmo saldo. Aí a solução é no nível do banco: lock otimista com `@Version`, lock pessimista com `SELECT ... FOR UPDATE`, ou uma atualização condicional atômica como `UPDATE conta SET saldo = saldo - ? WHERE id = ? AND saldo >= ?`."
