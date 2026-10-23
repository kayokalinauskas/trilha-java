---
pergunta: "O Kafka garante a ordem das mensagens? Como você garantiria que os eventos de uma conta sejam processados em ordem?"
tema: "Mensageria"
nivel: jr
idioma: pt
modulo: 12-mensageria
intencao: "Saber que a ordem é por partição e que a chave é a ferramenta para controlá-la."
pontosChave:
  - "Ordem só dentro de uma partição, não no tópico"
  - "A chave (id da conta) leva todas as mensagens da conta para a mesma partição"
  - "No consumer group, cada partição é lida por um consumidor só"
  - "Retries em outro tópico e aumento de partições quebram a ordem"
armadilha: "Dizer que o Kafka garante a ordem do tópico, ou propor um tópico com uma partição só para tudo."
---

"O Kafka garante a ordem **dentro de uma partição**, não no tópico inteiro. Um tópico com três partições tem três sequências independentes.

Para que os eventos de uma conta fiquem em ordem, uso o **id da conta como chave** da mensagem. O produtor calcula o hash da chave módulo o número de partições, então todos os eventos daquela conta caem na mesma partição, na ordem em que foram publicados. No consumo, cada partição é atribuída a um único consumidor do grupo, e ele processa sequencialmente.

Tem três armadilhas que eu cuidaria. **Retry não bloqueante**, com tópicos de retry, tira a mensagem que falhou da fila e deixa as seguintes passarem: para saldo, prefiro o retry bloqueante, mesmo segurando a partição. **Aumentar as partições** muda o resultado do módulo, e a conta passa a ir para outra partição durante a transição. E, do lado do produtor, se eu publicar via outbox, preciso publicar na ordem de criação.

Ordem global no tópico só com uma partição, o que mata o paralelismo. Quase sempre, o que o negócio precisa é ordem **por entidade**, e a chave resolve."
