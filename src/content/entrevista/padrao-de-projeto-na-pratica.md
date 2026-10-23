---
pergunta: "Qual padrão de projeto você mais usa? Mostre como aplicou num caso real."
tema: "Arquitetura"
nivel: jr
idioma: pt
modulo: 13-arquitetura
intencao: "Ver se o candidato conhece padrões pelo problema que resolvem, e não por definição decorada."
pontosChave:
  - "Começar pelo problema (uma regra que varia por tipo e cresce com o tempo)"
  - "Strategy com Spring: uma interface, um @Component por variação, List<Interface> injetada"
  - "Ganho concreto: uma variação nova é uma classe nova (aberto/fechado)"
  - "Reconhecer padrões do próprio framework: proxies (decorator), eventos (observer), JdbcTemplate (template method)"
armadilha: "Listar os 23 padrões do GoF sem nenhum exemplo, ou citar Singleton como o favorito."
---

"O que mais uso é o **Strategy**, sempre que tenho uma regra que varia por tipo e cresce com o tempo. Um exemplo: um cálculo de frete com um `switch` enorme, e cada transportadora nova era mais um `case` e um deploy do motor inteiro.

Criei uma interface `ModalidadeDeFrete`, com o método `cotar(pedido)`, e uma classe por modalidade, cada uma um `@Component`. O motor recebe `List<ModalidadeDeFrete>` no construtor. O Spring injeta todas, e ele só itera, aplica as promoções e ordena. Uma transportadora nova virou uma classe nova, sem mexer no motor. Isso é o princípio aberto/fechado na prática. Antes de refatorar, cobri o código antigo com um teste de caracterização, comparando o resultado dele com o novo em milhares de pedidos aleatórios.

Também tento reconhecer os padrões que o framework já usa por mim. O `@Transactional` funciona porque o Spring cria um proxy, um **decorator**, e por isso a chamada interna não abre transação. O `ApplicationEventPublisher` é um **observer**. O `JdbcTemplate` e o `RestClient` são **template method**, e hoje o passo variável é uma lambda."
