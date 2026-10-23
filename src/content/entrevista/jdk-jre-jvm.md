---
pergunta: "Qual a diferença entre JDK, JRE e JVM?"
tema: "Fundamentos"
nivel: jr
idioma: pt
modulo: 00-comece-aqui
intencao: "Se você entende o caminho do código até a execução, e não só decorou siglas."
pontosChave:
  - "javac compila .java em bytecode .class"
  - "A JVM executa bytecode e o JIT compila trechos quentes para código nativo"
  - "O JDK contém compilador, ferramentas e a JVM; o JRE separado deixou de existir no Java 11"
  - "Portabilidade: o mesmo bytecode roda em qualquer SO com JVM"
armadilha: "Dizer que Java é \"interpretado e por isso lento\", ignorando o JIT."
---

"A **JVM** é a máquina virtual que executa **bytecode**. O **JDK** é o kit de desenvolvimento: traz o compilador `javac`, ferramentas como `jshell` e `jar`, e a própria JVM. O **JRE** era o pacote só para executar, e desde o Java 11 não é mais distribuído separadamente.

O fluxo é: eu escrevo `.java`, o `javac` gera `.class` em bytecode, e a JVM carrega e executa esse bytecode. No começo ela interpreta, mas o **JIT** identifica os trechos mais executados e os compila para código nativo otimizado. Por isso uma aplicação Java fica mais rápida depois do *warm-up*. Essa separação é o que dá a portabilidade: o mesmo bytecode roda em Linux, Windows ou macOS."
