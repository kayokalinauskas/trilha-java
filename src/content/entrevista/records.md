---
pergunta: "O que são records? Quando você usaria um?"
tema: "POO"
nivel: jr
idioma: pt
modulo: 02-poo
intencao: "Atualização com Java moderno (16+)."
pontosChave:
  - "Classe imutável e transparente para dados"
  - "Gera construtor, acessores, equals, hashCode e toString"
  - "Construtor compacto para validar e normalizar"
  - "Cuidado com componentes mutáveis (copiar listas)"
  - "Bons para DTOs, value objects e eventos; não para entidades JPA"
---

"Records, oficiais desde o Java 16, são classes imutáveis feitas para carregar dados. A declaração `record Cep(String digitos)` gera campos `private final`, construtor, métodos de acesso, `equals`, `hashCode` e `toString`, todos baseados nos componentes.

Uso para DTOs de API, value objects como `Dinheiro` ou `Cep`, eventos e resultados de consultas. No **construtor compacto** eu valido e normalizo: por exemplo, removo o hífen do CEP. Um cuidado: o record torna os campos finais, mas não os objetos para os quais eles apontam; se tiver uma `List`, faço `List.copyOf` no construtor. E não uso records como entidades JPA, porque elas precisam de construtor sem argumentos e costumam ter estado mutável e identidade."
