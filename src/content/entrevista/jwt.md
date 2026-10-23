---
pergunta: "Como funciona a autenticação com JWT? Quais os cuidados?"
tema: "Segurança"
nivel: pleno
idioma: pt
modulo: 11-seguranca
intencao: "Segurança de APIs stateless na prática."
pontosChave:
  - "Token assinado (não criptografado): header.payload.assinatura"
  - "Servidor valida assinatura e expiração sem consultar sessão"
  - "Access token curto + refresh token"
  - "Revogação é difícil: expiração curta, denylist ou rotação de refresh"
  - "Não colocar dados sensíveis no payload; preferir RS256/ES256 e um provedor de identidade"
armadilha: "Achar que o conteúdo do JWT é secreto — ele é só Base64, qualquer um lê."
---

"Um JWT tem três partes: header, payload com as *claims* (quem é o usuário, papéis, expiração) e uma **assinatura**. O servidor valida a assinatura e a expiração e confia nas claims sem consultar uma sessão, o que torna a API **stateless** e fácil de escalar horizontalmente. No Spring Security, isso é o **OAuth2 Resource Server**: configuro o emissor e ele valida os tokens.

Cuidados: o payload **não é criptografado**, só codificado em Base64, então nada sensível vai nele. Uso *access tokens* de **vida curta** (minutos) e *refresh tokens* para renovar, com rotação. Revogação é o ponto fraco do modelo stateless: mitigo com expiração curta e, quando preciso revogar na hora, uma *denylist* por ID do token. Prefiro assinatura assimétrica (RS256 ou ES256), em que só o emissor tem a chave privada, e em projetos reais delego a emissão a um provedor de identidade (Keycloak, Auth0, Cognito) em vez de escrever o meu. No front-end web, o token fica em cookie `HttpOnly` e `Secure`, não no `localStorage`."
