---
pergunta: "Por que não devemos usar double para valores monetários? O que usar?"
tema: "Fundamentos"
nivel: jr
idioma: pt
modulo: 01-java-essencial
intencao: "Maturidade para sistemas financeiros — pergunta clássica em fintechs e bancos."
pontosChave:
  - "double usa base 2 (IEEE 754) e não representa 0,1 exatamente"
  - "Os erros se acumulam em somas e arredondamentos"
  - "BigDecimal criado a partir de String, com escala e RoundingMode explícitos"
  - "compareTo em vez de equals; ou long em centavos"
armadilha: "Usar new BigDecimal(0.1), que herda o erro do double."
---

"`double` segue o padrão IEEE 754, que representa números em **base 2**. Valores como 0,1 não têm representação exata, então `0.1 + 0.2` dá `0.30000000000000004`. Em uma soma de milhares de parcelas, esse erro se acumula e o total não fecha.

Para dinheiro, uso `BigDecimal`, sempre criado a partir de `String` (`new BigDecimal("19.90")`) ou `BigDecimal.valueOf`, nunca de `new BigDecimal(double)`. Toda divisão recebe escala e modo de arredondamento; em geral `HALF_EVEN`, o arredondamento bancário, a não ser que o negócio defina outro. Comparo com `compareTo`, porque `equals` considera a escala. Uma alternativa é guardar **centavos em `long`**, que é exata e rápida para soma e subtração. E, idealmente, encapsulo tudo em um value object `Dinheiro` com moeda."
