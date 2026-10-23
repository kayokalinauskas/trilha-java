---
pergunta: "O que você mocka em um teste unitário? O que não mocka?"
tema: "Testes"
nivel: jr
idioma: pt
modulo: 10-testes
intencao: "Critério na escrita de testes, evitando testes frágeis."
pontosChave:
  - "Mockar dependências externas e lentas (gateway, e-mail, outros serviços)"
  - "Não mockar o objeto sob teste nem value objects"
  - "Preferir fakes simples a mocks complexos quando possível"
  - "Verificar comportamento observável, não detalhes de implementação"
  - "Mock demais = teste acoplado ao código"
---

"No teste unitário, isolo a unidade que estou testando das dependências que são **lentas, não determinísticas ou externas**: o gateway de pagamento, o envio de e-mail, o relógio, outro microsserviço. Para isso uso Mockito, ou um *fake* simples, como um repositório em memória, que costuma deixar o teste mais legível.

Não mocko o próprio objeto sob teste, nem value objects e entidades de domínio (uso os reais), nem bibliotecas que não controlo — para essas, crio um adaptador meu e mocko o adaptador.

E tento verificar **comportamento observável**: o retorno, o estado final, a mensagem enviada. Testes cheios de `verify(...)` sobre cada chamada interna quebram a cada refatoração, mesmo quando o comportamento não mudou. Se eu preciso de muitos mocks para testar uma classe, geralmente é um sinal de que ela tem responsabilidades demais."
