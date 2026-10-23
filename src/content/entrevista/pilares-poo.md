---
pergunta: "Quais são os pilares da orientação a objetos? Dê exemplos práticos."
tema: "POO"
nivel: jr
idioma: pt
modulo: 02-poo
intencao: "Se você vai além da definição de livro e consegue ligar cada pilar a uma decisão de código."
pontosChave:
  - "Encapsulamento: proteger invariantes, não só gerar getters/setters"
  - "Abstração: expor o essencial por contratos (interfaces)"
  - "Herança: reuso por especialização — com cuidado (LSP)"
  - "Polimorfismo: tratar implementações diferentes de forma uniforme"
armadilha: "Definir encapsulamento como \"atributos private com getters e setters\"."
---

"São quatro. **Encapsulamento** é o objeto proteger as próprias regras: por exemplo, uma `ContaCorrente` sem `setSaldo`, em que o saldo só muda por `depositar` e `sacar`, que validam o limite. **Abstração** é expor só o essencial, geralmente por uma interface como `MeioDePagamento`, escondendo se é Pix ou cartão. **Herança** é especializar uma classe existente; eu uso com cuidado, porque acopla a subclasse aos detalhes da superclasse e pode violar o Princípio de Liskov, então muitas vezes prefiro composição. **Polimorfismo** é poder chamar `pagamento.autorizar()` sem saber o tipo concreto, e cada implementação responde do seu jeito. Na prática, é o que me permite adicionar um meio de pagamento novo criando uma classe, sem mexer no checkout."
