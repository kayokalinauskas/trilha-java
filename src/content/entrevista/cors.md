---
pergunta: "O que é CORS? Ele protege a sua API de chamadas indevidas?"
tema: "Segurança"
nivel: jr
idioma: pt
modulo: 11-seguranca
intencao: "Desfazer o mito mais comum sobre CORS."
pontosChave:
  - "Regra do navegador: quais origens podem LER respostas da API"
  - "Não protege a API: curl, Postman e servidores ignoram CORS"
  - "Configurar origens exatas; nunca * com credenciais"
  - "Preflight OPTIONS para métodos e headers não simples"
armadilha: "Achar que configurar CORS restritivo substitui autenticação."
---

"CORS é um mecanismo do **navegador**. Por padrão, um script em `app.exemplo.com` não consegue ler a resposta de uma chamada para `api.exemplo.com`. A API autoriza origens específicas com os headers `Access-Control-Allow-*`. Para métodos e headers não simples, o navegador faz antes um *preflight* com `OPTIONS`.

E não, ele não protege a API. Ele protege o **usuário**: impede que um site malicioso aberto no navegador dele leia dados da sua API usando as credenciais dele. `curl`, Postman, um script ou outro servidor ignoram CORS completamente. A API continua precisando de autenticação e autorização em todas as rotas.

Na configuração, uso origens exatas e nunca `*` junto com credenciais, que o navegador e o Spring recusam. Refletir de volta qualquer `Origin` recebido equivale a `*`. No Spring Security, ativo `.cors()` na cadeia para o preflight passar sem token."
