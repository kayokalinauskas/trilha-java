---
pergunta: "Por que String é imutável? E quando usar StringBuilder?"
tema: "Fundamentos"
nivel: jr
idioma: pt
modulo: 01-java-essencial
intencao: "Conhecimento de fundamentos da JDK e noção de custo de alocação."
pontosChave:
  - "Segurança: não muda depois de validada"
  - "Thread-safe e cacheável (string pool)"
  - "Hash estável para uso como chave"
  - "Concatenar com + em laço é O(n²); StringBuilder reaproveita o buffer"
  - "StringBuffer é a versão sincronizada, raramente necessária"
---

"`String` é imutável por segurança e desempenho. Por segurança, porque Strings são usadas em nomes de arquivo, URLs, credenciais e chaves: se pudessem mudar depois de validadas, abririam brechas. Por desempenho, porque objetos imutáveis podem ser compartilhados livremente entre threads e reaproveitados no **string pool**, e o `hashCode` pode ser calculado uma vez, o que é ótimo para chaves de `HashMap`.

O efeito colateral é que toda "modificação" cria uma String nova. Concatenar com `+=` dentro de um laço copia o conteúdo inteiro a cada volta, um custo quadrático. Nesses casos uso `StringBuilder`, que é mutável e cresce um buffer. O `StringBuffer` faz o mesmo com sincronização; hoje quase não faz sentido, porque o builder costuma ser uma variável local, sem concorrência."
