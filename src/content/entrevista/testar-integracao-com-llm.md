---
pergunta: "Como você testa um código que chama um LLM, se a resposta muda a cada chamada?"
tema: "IA no backend"
nivel: jr
idioma: pt
modulo: 16-ia-backend
intencao: "Separar o que é determinístico (o seu código) do que não é (o modelo) e saber testar cada parte."
pontosChave:
  - "Testes unitários com um ChatModel falso e roteirizado: sem rede, sem custo, previsíveis"
  - "Testar o prompt enviado (o que vai e o que não vai), o parsing, a validação e o fallback"
  - "Simular as falhas: JSON inválido, valor fora do domínio, timeout, 429"
  - "Qualidade do modelo: avaliação (evals) com um conjunto de casos conhecidos, fora do build normal"
  - "Temperatura baixa e saída estruturada reduzem a variação"
---

"Eu separo em duas perguntas: o **meu código** está certo? E o **modelo** responde bem?

Para o meu código, os testes são unitários e determinísticos. Troco o `ChatModel` do Spring AI por um falso, que devolve respostas roteirizadas e **guarda o prompt** que recebeu. Assim, eu testo o que realmente é responsabilidade minha: o prompt tem as instruções certas? O dado do usuário foi delimitado? Um CPF foi mascarado antes de sair? E, principalmente, o que acontece quando o modelo responde mal: JSON quebrado, uma categoria que não existe, uma confiança fora da faixa, um timeout ou um 429. Esses são os casos que derrubam produção, e com um modelo falso eles são fáceis de simular. Um detalhe: no Spring AI, para as ferramentas funcionarem com um modelo falso, ele precisa devolver `ToolCallingChatOptions` nas opções.

A qualidade do modelo eu meço de outro jeito, com **avaliação**: um conjunto de entradas com a resposta esperada (um *golden set*), rodado contra o modelo real, com uma taxa de acerto mínima. Isso não roda a cada commit, porque custa dinheiro e varia um pouco, mas roda antes de trocar de modelo, de mudar o prompt ou de atualizar a versão, e aí eu comparo os números. Temperatura baixa e saída estruturada ajudam a deixar essa medição estável."
