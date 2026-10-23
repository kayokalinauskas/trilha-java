---
pergunta: "Como você protegeria o endpoint de login contra ataques de força bruta?"
tema: "Segurança"
nivel: pleno
idioma: pt
modulo: 11-seguranca
intencao: "Defesa em camadas e noção de sistemas distribuídos."
pontosChave:
  - "Bloqueio temporário por conta após N falhas (não permanente: vira DoS contra o usuário)"
  - "Rate limiting por IP (token bucket), com 429 e Retry-After"
  - "Com várias instâncias, estado no Redis ou limite no gateway"
  - "MFA / passkeys; eventos de segurança logados com alerta"
  - "Mesma resposta e mesmo tempo para evitar enumeração"
armadilha: "Propor só o bloqueio de conta — credential stuffing testa uma senha em milhares de contas diferentes."
---

"Em camadas, porque cada defesa cobre um ataque diferente.

Contra o ataque a **uma conta**, bloqueio temporário: cinco falhas seguidas bloqueiam por 15 minutos. Temporário porque um bloqueio permanente deixaria qualquer um travar a conta de qualquer pessoa.

Mas o ataque mais comum hoje é o *credential stuffing*: senhas vazadas de outros sites testadas em milhares de contas, uma tentativa em cada. O bloqueio por conta não pega. Para isso, rate limiting por **origem**: um token bucket por IP no `POST /login`, respondendo 429 com `Retry-After`. Como é um filtro antes do controller, a requisição barrada nem chega ao BCrypt.

Com várias instâncias da aplicação, um limitador em memória multiplica o limite pelo número de réplicas. Então o estado vai para o Redis, com Bucket4j por exemplo, ou o limite sobe para o gateway. Atrás de proxy, cuidado com o IP: só confio no `X-Forwarded-For` se ele vier do meu proxy.

Completando: MFA ou passkeys para quem aceita, a mesma resposta e o mesmo tempo para e-mail inexistente e senha errada, e eventos de login com falha logados, com alerta para picos."
