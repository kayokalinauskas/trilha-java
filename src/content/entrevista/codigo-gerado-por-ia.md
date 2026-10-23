---
pergunta: "Você usa assistentes de código com IA? Como garante a qualidade do que eles geram?"
tema: "IA no backend"
nivel: jr
idioma: pt
modulo: 16-ia-backend
intencao: "Maturidade no uso de ferramentas: produtividade sem terceirizar o julgamento nem a responsabilidade."
pontosChave:
  - "Sim, para tarefas bem definidas: boilerplate, testes, explorar uma API, refatorar"
  - "Eu sou responsável pelo que faço commit: leio e entendo cada linha"
  - "Erros típicos: API desatualizada ou inventada, dependência inexistente, segurança (autorização, SQL, segredos)"
  - "Testes a partir da regra de negócio, e não da implementação gerada"
  - "Política da empresa: nada de segredos ou dados de clientes no prompt"
armadilha: "Responder 'não uso' (soa desatualizado) ou 'uso para tudo e confio' (soa irresponsável). A resposta boa mostra critério."
---

"Uso, sim, no dia a dia. Funciona bem para tarefas bem definidas: código repetitivo, um primeiro rascunho de testes, entender uma API nova, refatorar um trecho com testes em volta. Onde a decisão é de arquitetura ou de regra de negócio, eu uso mais como alguém para discutir do que para escrever.

A regra que sigo: **eu sou responsável** por tudo que vai no meu commit, então leio e entendo cada linha, como faria com o PR de um colega. Eu sei onde esses assistentes costumam errar. Eles sugerem **APIs desatualizadas**, porque aprenderam com código antigo: `javax.*` num projeto Boot 4, uma classe do Spring AI que foi renomeada. Às vezes inventam um método ou uma dependência que não existe. E, principalmente, erram em **segurança**: pegam o id do usuário da URL em vez do token, concatenam SQL, deixam uma chave no código, engolem exceções.

Por isso, os testes eu escrevo, ou pelo menos reviso, a partir da **regra de negócio**, e não do código gerado. Um teste gerado a partir da implementação confirma os bugs dela. E sigo a política da empresa: nada de segredos ou dados de clientes no prompt."
