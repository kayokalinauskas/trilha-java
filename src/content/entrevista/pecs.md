---
pergunta: "O que é PECS em generics? Por que List<Integer> não é uma List<Number>?"
tema: "Generics"
nivel: pleno
idioma: pt
modulo: 03-colecoes-generics-excecoes
intencao: "Profundidade em Java: invariância de generics e uso de wildcards."
pontosChave:
  - "Generics são invariantes para proteger a segurança de tipos"
  - "Exemplo do add(3.14) em lista de inteiros"
  - "Producer Extends, Consumer Super"
  - "Collections.copy e Comparable<? super T>"
---

"Se `List<Integer>` fosse uma `List<Number>`, eu poderia atribuí-la a uma variável `List<Number>` e fazer `add(3.14)`, colocando um `Double` numa lista de inteiros. Quem lesse a lista original tomaria um `ClassCastException`. Por isso generics são **invariantes**.

Para ter flexibilidade, uso wildcards seguindo o **PECS**: *Producer Extends, Consumer Super*. Se o parâmetro só **fornece** valores que eu leio, uso `? extends T`: um `somar(List<? extends Number>)` aceita listas de `Integer`, `Double` ou `BigDecimal`, mas não deixa adicionar. Se o parâmetro só **recebe** valores que eu escrevo, uso `? super T`. O exemplo da própria JDK é `Collections.copy(List<? super T> destino, List<? extends T> origem)`. Outro caso é `Comparable<? super T>` em métodos como `max`, necessário para tipos como `LocalDate`, que implementa `Comparable<ChronoLocalDate>`."
