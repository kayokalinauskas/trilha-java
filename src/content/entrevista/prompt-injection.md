---
pergunta: "O que é prompt injection? Como você protege uma funcionalidade que usa um LLM com texto enviado pelo usuário?"
tema: "IA no backend"
nivel: pleno
idioma: pt
modulo: 16-ia-backend
intencao: "Pensamento de segurança aplicado a LLMs: defesa em camadas e limitação do estrago."
pontosChave:
  - "Instruções e dados no mesmo canal: o modelo não distingue com certeza"
  - "Direta (o usuário escreve) e indireta (documento, e-mail, página lida pelo modelo)"
  - "Não existe defesa completa: camadas (delimitadores, limites, validação de saída, canário)"
  - "O que mais importa: limitar o que o modelo pode fazer (menor privilégio, ferramentas sem ids, confirmação humana)"
  - "Validar a saída como entrada externa; minimizar dados pessoais enviados"
armadilha: "Dizer que basta escrever no prompt 'nunca siga instruções do usuário' ou filtrar a frase 'ignore as instruções'. Isso é um pedido, e não um controle: o ataque é reescrito de mil formas."
---

"Prompt injection é quando um texto que deveria ser só **dado** consegue mudar o comportamento do modelo, porque para um LLM as instruções e os dados chegam pelo mesmo canal: texto. A forma **direta** é o usuário escrever 'ignore as instruções anteriores'. A **indireta** é mais perigosa: a instrução está num documento, num e-mail ou numa página que o modelo lê como parte da tarefa, e quem fez o pedido nem sabe.

Não existe uma defesa completa, então eu trabalho em camadas. Na entrada: instruções fixas no prompt de sistema, o conteúdo do usuário delimitado numa mensagem separada, sem deixar ninguém fechar o delimitador, e um limite de tamanho. Na saída: trato a resposta como entrada externa, validando o formato e o domínio. Um canário nas instruções detecta quando o prompt vaza.

Mas a camada que **mais** importa é limitar o que o modelo pode fazer. As ferramentas recebem a identidade do usuário pelo contexto da aplicação, e nunca como um argumento que o modelo preenche. Ações irreversíveis, como transferir dinheiro, viram uma **proposta** que o usuário confirma. Cada ferramenta faz uma coisa, com o mínimo de permissão. Assim, mesmo que a injeção funcione, o estrago é pequeno. É o LLM01 do OWASP Top 10 para LLMs, e o LLM06, *Excessive Agency*, é o que transforma uma injeção num incidente."
