---
pergunta: "O que é SQL injection e como prevenir em Java?"
tema: "Segurança"
nivel: jr
idioma: pt
modulo: 11-seguranca
intencao: "Conhecimento básico do OWASP Top 10."
pontosChave:
  - "Concatenar entrada do usuário em SQL permite alterar a query"
  - "PreparedStatement / parâmetros nomeados em JPQL"
  - "Spring Data e JPA parametrizam por padrão — cuidado com queries nativas montadas com +"
  - "Validar entrada e usar menor privilégio no usuário do banco"
---

"SQL injection acontece quando a entrada do usuário é **concatenada** na query. Com `"SELECT * FROM usuario WHERE email = '" + email + "'"`, se alguém enviar `' OR '1'='1`, a condição vira sempre verdadeira e a query devolve todos os usuários; com variações, dá para apagar tabelas.

A prevenção é **nunca concatenar**: usar `PreparedStatement` com `?`, ou parâmetros nomeados no JPQL (`where u.email = :email`). O banco trata o valor como dado, nunca como código SQL. Spring Data JPA e JdbcTemplate parametrizam por padrão; o risco aparece quando alguém monta query nativa ou `ORDER BY` dinâmico com `+`. Para campos de ordenação vindos do usuário, uso uma lista branca de colunas permitidas. Como defesa em profundidade, o usuário do banco da aplicação tem só as permissões necessárias."
