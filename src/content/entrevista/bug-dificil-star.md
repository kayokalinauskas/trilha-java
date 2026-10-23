---
pergunta: "Conte sobre um bug difícil que você resolveu."
tema: "Comportamental"
nivel: jr
idioma: pt
modulo: 17-carreira
intencao: "Método de investigação, persistência e aprendizado — além de comunicação estruturada."
pontosChave:
  - "Estrutura STAR: Situação, Tarefa, Ação, Resultado"
  - "Mostrar o MÉTODO: reproduzir, evidências, hipóteses, causa raiz"
  - "Números e impacto concretos"
  - "O que você mudou para não acontecer de novo (teste, alerta, processo)"
armadilha: "Contar uma história vaga, sem mostrar o seu raciocínio, ou culpar colegas."
---

Use a estrutura **STAR** e mostre o **método**. Um exemplo com um exercício da trilha, se você ainda não tiver experiência profissional:

"**Situação**: no simulador de parcelamento que eu construí, a soma das parcelas às vezes dava um centavo a menos que o total. **Tarefa**: eu precisava garantir que o total sempre fechasse, para qualquer valor e número de parcelas. **Ação**: primeiro escrevi um teste que reproduzia o erro, testando todas as quantidades de 1 a 24 parcelas; ele falhava em 7 casos. Investiguei com o debugger e vi que eu arredondava cada parcela separadamente, então os arredondamentos se acumulavam. Mudei a abordagem para trabalhar em centavos inteiros e jogar a sobra da divisão na primeira parcela. **Resultado**: os 24 casos passaram, e o teste ficou na suíte para o bug não voltar. Aprendi a sempre testar as bordas e a nunca arredondar no meio de um cálculo financeiro."

O entrevistador quer ver que você **investiga** em vez de adivinhar, e que aprende com o problema.
