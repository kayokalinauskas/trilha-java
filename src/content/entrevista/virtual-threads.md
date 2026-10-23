---
pergunta: "O que são virtual threads? Quando NÃO usá-las?"
tema: "Concorrência"
nivel: pleno
idioma: pt
modulo: 06-concorrencia
intencao: "Se você entende o Project Loom de verdade ou repete termos da moda."
pontosChave:
  - "Threads de plataforma são 1:1 com o SO e caras; virtuais são leves e gerenciadas pela JVM"
  - "Ao bloquear em I/O, a virtual thread libera a carrier thread"
  - "Ganho em throughput de tarefas I/O-bound, não em CPU"
  - "Não fazer pool de virtual threads; limitar recursos com semáforo"
  - "Desde o Java 24, synchronized não fixa mais a carrier (JEP 491)"
armadilha: "Dizer que virtual threads deixam o código mais rápido em processamento pesado de CPU."
---

"Threads de plataforma são 1:1 com threads do sistema operacional: cada uma reserva uma pilha grande e a troca de contexto é cara, então um servidor com um pool de 200 threads fica limitado a ~200 requisições simultâneas bloqueadas em I/O. **Virtual threads**, oficiais desde o Java 21, são gerenciadas pela JVM: custam poucos kilobytes e dá para ter milhões. Quando uma virtual thread bloqueia em I/O — uma query ou uma chamada HTTP —, a JVM a desmonta da *carrier thread*, que fica livre para rodar outra.

O ganho é de **throughput** em cargas I/O-bound, com código síncrono e simples. No Spring Boot, basta `spring.threads.virtual.enabled=true`.

Quando não ajudam: tarefas **CPU-bound**, como criptografia ou processamento de imagens, porque o limite continua sendo o número de núcleos. Cuidados: não faço pool de virtual threads (elas são baratas; crio uma por tarefa) e, se preciso limitar o acesso a um recurso escasso, como conexões de banco, uso um semáforo ou o próprio pool de conexões. O problema de *pinning* com `synchronized` foi resolvido no Java 24; ainda há pinning em chamadas nativas."
