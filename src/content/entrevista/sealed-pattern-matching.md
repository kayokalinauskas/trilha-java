---
pergunta: "Explique sealed interfaces e pattern matching no switch. Quando preferir isso a polimorfismo?"
tema: "POO"
nivel: pleno
idioma: pt
modulo: 02-poo
intencao: "Domínio do Java 17–21 e maturidade de design (problema da expressão)."
pontosChave:
  - "sealed ... permits fecha a hierarquia"
  - "switch exaustivo sem default: nova alternativa quebra a compilação"
  - "Record patterns e guardas when"
  - "Polimorfismo quando surgem novos tipos; sealed quando surgem novas operações"
armadilha: "Colocar default em switch sobre tipo sealed, perdendo a verificação de exaustividade."
---

"Uma `sealed interface` declara, com `permits`, todas as implementações possíveis. Com isso, um `switch` sobre ela pode ser **exaustivo**: eu trato `case Pix p`, `case Cartao c`, `case Boleto b` sem `default`, e se alguém adicionar `Cripto` ao `permits`, todo switch que não a trata **para de compilar**. O compilador vira um checklist das regras de negócio. Desde o Java 21 dá para usar **record patterns**, desestruturando os componentes no próprio `case`, e **guardas** com `when`.

A escolha em relação ao polimorfismo é o chamado problema da expressão. Se novos **tipos** aparecem com frequência, polimorfismo é melhor, porque cada tipo novo é só uma classe nova. Se os tipos são estáveis e novas **operações** aparecem o tempo todo, como calcular taxa, gerar comprovante e mapear para a API sobre os mesmos meios de pagamento, sealed com switch concentra cada regra em um lugar só."
