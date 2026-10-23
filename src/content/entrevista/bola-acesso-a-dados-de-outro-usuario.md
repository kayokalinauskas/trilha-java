---
pergunta: "Como você impede que um usuário acesse os dados de outro na sua API?"
tema: "Segurança"
nivel: pleno
idioma: pt
modulo: 11-seguranca
intencao: "Autorização no nível do objeto — a falha nº 1 do OWASP API Security Top 10."
pontosChave:
  - "Autenticação identifica; a posse do recurso é verificada em cada acesso por id (BOLA/IDOR)"
  - "A verificação mora no caso de uso, com o solicitante explícito — testável sem HTTP"
  - "Listagens filtradas na consulta (WHERE dono = ?), não em memória"
  - "404 para recurso de outra pessoa, para não confirmar que o id existe"
  - "UUID não é controle de acesso; testes com dois usuários em cada rota"
armadilha: "Dizer que o Spring Security resolve sozinho, ou que usar UUID em vez de id sequencial basta."
---

"O Spring Security resolve a autenticação: eu sei quem é o usuário pelo token. Mas quem garante que a conta 123 é **dele** é o meu código, em cada acesso. Esse é o BOLA, o primeiro item do OWASP API Security Top 10, e aparece justamente porque o código parece certo: tem login, tem validação.

Na prática, o controller transforma o token num objeto `Solicitante` e passa para o caso de uso. O serviço busca o recurso e filtra: `contas.buscar(id).filter(solicitante::podeAcessar).orElseThrow(...)`. Uso 404, e não 403, para a conta de outra pessoa: assim 'não existe' e 'não é sua' ficam indistinguíveis. Nas listagens, o filtro vai na consulta, `WHERE dono_id = ?`, e a paginação vem certa.

Trocar ids sequenciais por UUID ajuda contra enumeração, mas não é controle de acesso: ids vazam em logs e links. E eu testo com dois usuários em todas as rotas que recebem um id, porque é aí que o bug volta quando alguém cria uma rota nova."
