---
pergunta: "Herança ou composição? Dê um exemplo em que a herança causou problema."
tema: "POO"
nivel: pleno
idioma: pt
modulo: 02-poo
intencao: "Experiência de design: você reconhece acoplamento e o Princípio de Substituição de Liskov."
pontosChave:
  - "Herança acopla a subclasse aos detalhes da superclasse (classe base frágil)"
  - "LSP: a subclasse deve funcionar onde a superclasse funciona"
  - "Composição permite trocar comportamento em tempo de execução"
  - "Exemplo concreto de refatoração"
---

"Prefiro composição na maioria dos casos. Herança cria um acoplamento forte: a subclasse depende de detalhes internos da superclasse, e uma mudança lá pode quebrar tudo aqui — o problema da classe base frágil. Além disso, a relação "é um" do mundo real nem sempre vale para o comportamento: o exemplo clássico é `Quadrado extends Retangulo`, que quebra quem espera poder mudar largura e altura de forma independente. Isso viola o Princípio de Substituição de Liskov.

Um exemplo prático: uma hierarquia `Funcionario → VendedorCLT, VendedorPJ, GerenteCLT...` explode em combinações. Refatorando para composição, `Funcionario` passa a ter um `Cargo` e um `Contrato`, que são interfaces. Novas combinações não exigem novas classes, e trocar o contrato de alguém é trocar um objeto. Uso herança quando a relação é estável, eu controlo as duas classes e a superclasse foi pensada para ser estendida."
