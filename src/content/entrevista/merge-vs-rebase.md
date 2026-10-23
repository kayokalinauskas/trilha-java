---
pergunta: "Merge ou rebase? Como é o fluxo de Git do seu time?"
tema: "DevOps"
nivel: jr
idioma: pt
modulo: 14-devops
intencao: "Mostrar que você trabalha bem num repositório compartilhado, sem reescrever o histórico dos outros."
pontosChave:
  - "Merge preserva o histórico como aconteceu e cria um commit de merge"
  - "Rebase reaplica seus commits sobre a base: histórico linear, mas com hashes novos"
  - "Regra de ouro: não fazer rebase de branch que outras pessoas já usam"
  - "Branches curtas e PR pequeno reduzem conflitos mais do que qualquer comando"
  - "Squash merge no PR e Conventional Commits deixam a main legível"
armadilha: "Dizer que 'rebase é sempre melhor' (ou o contrário), ou dar git push --force numa branch compartilhada."
---

"Os dois integram mudanças, com efeitos diferentes no histórico. O **merge** junta duas linhas e cria um commit de merge: o histórico mostra exatamente o que aconteceu, com as bifurcações. O **rebase** pega os meus commits e os reaplica em cima da base atual. O histórico fica linear, mas os commits ganham hashes novos, ou seja, estou reescrevendo o histórico.

Por isso a minha regra é: **rebase na minha branch local, antes de abrir ou atualizar o PR**, para trazer a `main` e resolver os conflitos do meu lado. Nunca faço rebase de uma branch que outra pessoa já puxou. Se precisar forçar o push da minha branch, uso `--force-with-lease`, que recusa se alguém tiver empurrado algo que eu não vi.

No time, o que mais evita dor não é o comando, é o tamanho. Trabalhamos com **branches curtas** (trunk-based, com poucos dias de vida), PRs pequenos e revisados, CI verde como condição para o merge e **squash merge**. Cada PR vira um commit na `main`, com mensagem no padrão Conventional Commits (`feat:`, `fix:`), o que ainda ajuda a gerar changelog e versão. Para funcionalidades grandes, prefiro feature flags a uma branch que vive um mês."
