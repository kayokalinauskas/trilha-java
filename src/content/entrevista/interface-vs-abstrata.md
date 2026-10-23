---
pergunta: "Quando usar interface e quando usar classe abstrata?"
tema: "POO"
nivel: jr
idioma: pt
modulo: 02-poo
intencao: "Capacidade de decidir design com critério, não por gosto."
pontosChave:
  - "Interface: contrato, múltipla implementação, sem estado"
  - "Classe abstrata: estado e implementação compartilhados, herança simples"
  - "default methods permitem evoluir interfaces"
  - "Na dúvida, interface (mais flexível e testável)"
---

"Uso **interface** para definir um **contrato**: o que uma classe sabe fazer. Uma classe pode implementar várias, interfaces não têm estado de instância, e desde o Java 8 podem ter métodos `default` e `static`, o que permite evoluí-las sem quebrar quem já as implementa.

Uso **classe abstrata** quando várias classes muito próximas compartilham **estado e parte da implementação**, como um *template method* em que o esqueleto do algoritmo é fixo e só alguns passos variam. O custo é que Java só permite herdar de uma classe.

Na dúvida, começo com interface, porque é mais flexível, facilita testes com mocks e combina com composição. É também o estilo predominante em aplicações Spring."
