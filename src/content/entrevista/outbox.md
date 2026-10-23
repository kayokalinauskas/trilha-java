---
pergunta: "O que é o Transactional Outbox Pattern e que problema ele resolve?"
tema: "Mensageria"
nivel: pleno
idioma: pt
modulo: 12-mensageria
intencao: "Conhecimento do problema de dual write em sistemas orientados a eventos."
pontosChave:
  - "Dual write: salvar no banco e publicar no broker não é atômico"
  - "Gravar o evento numa tabela outbox na MESMA transação"
  - "Um relay (polling ou CDC com Debezium) publica no broker"
  - "Entrega at-least-once: consumidores precisam ser idempotentes"
  - "Alternativa: Spring Modulith event publication registry"
armadilha: "Dizer que o outbox garante entrega \"exatamente uma vez\" — ele garante pelo menos uma vez."
---

"O problema é o **dual write**: eu salvo o pedido no banco e depois publico `PedidoCriado` no Kafka. Se a aplicação cair entre os dois passos, ou o broker estiver fora, o banco tem o pedido e o evento nunca sai. Se eu inverto a ordem, posso publicar um evento de um pedido que sofreu rollback.

O **outbox** resolve gravando o evento em uma tabela `outbox` **na mesma transação** do pedido. Ou os dois são gravados, ou nenhum. Um processo separado, o *relay*, lê a tabela e publica no broker: pode ser um job de polling ou **CDC** com Debezium lendo o log de transações do banco. Depois de publicado, o evento é marcado como enviado.

Isso garante entrega **pelo menos uma vez**, não exatamente uma vez: se o relay publicar e cair antes de marcar, publica de novo. Por isso os consumidores precisam ser idempotentes. No ecossistema Spring, o Spring Modulith tem um *event publication registry* que implementa essa ideia."
