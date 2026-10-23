---
pergunta: "Como você guardaria as senhas dos usuários de um sistema?"
tema: "Segurança"
nivel: jr
idioma: pt
modulo: 11-seguranca
intencao: "Saber o básico que evita um vazamento virar catástrofe."
pontosChave:
  - "Hash adaptativo com salt: Argon2id ou BCrypt; nunca criptografia reversível nem SHA puro"
  - "No Spring: DelegatingPasswordEncoder, com o algoritmo no prefixo e migração via upgradeEncoding"
  - "Política por tamanho e lista de senhas vazadas (NIST), sem composição forçada"
  - "Login com a mesma resposta e o mesmo tempo para e-mail inexistente e senha errada"
armadilha: "Responder 'criptografo com AES' ou 'uso SHA-256 com salt' — reversível ou rápido demais."
---

"Com um hash **adaptativo** e com salt: Argon2id, que é a primeira recomendação da OWASP, ou BCrypt. O ponto é que eles são lentos de propósito, com um custo ajustável. SHA-256 é rápido demais: uma GPU testa bilhões de senhas por segundo. E criptografia é reversível, então quem rouba o banco costuma levar a chave junto.

No Spring, uso o `DelegatingPasswordEncoder`: o hash fica salvo com o prefixo do algoritmo, tipo `{bcrypt}$2a$10$...`. Isso permite trocar de algoritmo depois, porque no login com sucesso o `upgradeEncoding` diz se o hash é antigo e eu regravo com o atual.

Dois cuidados que costumam ficar de fora. O BCrypt só usa os primeiros 72 bytes da senha, então a política precisa limitar o tamanho. E no login, e-mail inexistente e senha errada devolvem a mesma mensagem e levam o mesmo tempo: comparo com um hash fictício quando o usuário não existe, para não permitir enumeração de contas."
