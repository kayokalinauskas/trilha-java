---
pergunta: "O que é RAG? Quando você usaria RAG em vez de fine-tuning para um assistente com os documentos da empresa?"
tema: "IA no backend"
nivel: pleno
idioma: pt
modulo: 16-ia-backend
intencao: "Entender a arquitetura de um sistema com LLM e escolher a técnica pelo problema, e não pela moda."
pontosChave:
  - "RAG: buscar trechos relevantes (embeddings + busca vetorial) e colocá-los no prompt"
  - "Documentos que mudam, citação de fontes e controle de acesso por usuário favorecem RAG"
  - "Fine-tuning ensina formato, tom ou tarefa, não fatos atualizados"
  - "Filtro por tenant/permissão na própria consulta ao vector store"
  - "Limiar de similaridade e avaliação com perguntas reais"
armadilha: "Dizer que fine-tuning 'ensina os documentos ao modelo' e que resolve alucinação: ele muda o comportamento, mas os fatos ficam desatualizados e não dá para citar a fonte nem aplicar permissão por usuário."
---

"RAG é *retrieval-augmented generation*: antes de chamar o modelo, eu **busco** os trechos dos documentos que têm a ver com a pergunta e coloco esses trechos no prompt, com a instrução de responder com base neles. Na ingestão, os documentos são divididos em trechos, cada trecho vira um **embedding** e vai para um banco vetorial, como o pgvector. Na pergunta, gero o embedding dela e busco os trechos mais próximos pelo cosseno.

Para documentos da empresa, eu começo quase sempre por RAG. Os documentos mudam: basta reindexar, sem treinar nada. Dá para **citar a fonte** de cada resposta. E dá para aplicar **permissão**: o filtro por cliente ou por perfil vai na própria consulta ao vector store, então ninguém recebe um trecho que não poderia ler. Com fine-tuning, o conhecimento fica misturado nos pesos do modelo, sem controle de acesso e sem fonte.

Fine-tuning serve para outra coisa: ensinar um **formato**, um tom ou uma tarefa muito específica que o prompt não resolve bem. Muitas vezes os dois se combinam.

Na prática, os cuidados do RAG estão na recuperação: o tamanho dos trechos, um **limiar** mínimo de similaridade, para que o modelo diga 'não encontrei' em vez de inventar a partir de um contexto ruim, busca híbrida para códigos exatos e uma avaliação com perguntas reais, porque sem medir não dá para saber se uma mudança melhorou ou piorou."
