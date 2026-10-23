---
pergunta: "Como você escreveria o Dockerfile de uma aplicação Spring Boot para produção?"
tema: "DevOps"
nivel: jr
idioma: pt
modulo: 14-devops
intencao: "Ir além do Dockerfile que funciona: tamanho, cache de build, segurança e desligamento correto."
pontosChave:
  - "Multi-stage: a imagem final tem só o JRE e a aplicação"
  - "Jar extraído em camadas: dependências antes do código, para aproveitar o cache"
  - "Tag fixa na imagem base, nunca latest"
  - "USER sem privilégios"
  - "ENTRYPOINT em formato exec: o java é o PID 1 e recebe o SIGTERM"
  - "-XX:MaxRAMPercentage para o heap respeitar o limite do container"
armadilha: "Um único estágio com JDK e Maven, rodando como root, com ENTRYPOINT em formato shell. Funciona na demo, mas a imagem fica enorme, cada commit baixa todas as dependências de novo, e o docker stop corta requisições no meio."
---

"Começo pelo que vai para a imagem: o jar que o CI já testou com `mvn verify`. Não compilo dentro do Dockerfile, para não testar uma coisa e publicar outra.

O Dockerfile tem dois estágios. No primeiro, extraio o jar em camadas com `java -Djarmode=tools -jar app.jar extract --layers`. No segundo, parto de uma imagem **só com JRE** e de tag fixa, por exemplo `eclipse-temurin:25-jre-noble`, nunca `latest`. Copio as camadas em ordem: **dependências primeiro, código por último**. Como as dependências quase nunca mudam, um commit reconstrói só a última camada, de poucos KB, e o push fica rápido.

Depois vêm três detalhes de produção:
- **`USER`** sem privilégios: se alguém explorar a aplicação, não vira root no container.
- **`ENTRYPOINT ["java", "-jar", "app.jar"]`**, em formato exec. O java vira o PID 1 e recebe o `SIGTERM` do `docker stop` ou do Kubernetes, e o Spring faz o desligamento gracioso. Em formato shell, quem recebe o sinal é o `sh`, que não repassa.
- **`-XX:MaxRAMPercentage=75`**: o heap é dimensionado pela memória do container, e sobra espaço para metaspace e threads.

Acrescento um `.dockerignore` para `.git` e `.env` não entrarem no contexto. Como alternativa sem Dockerfile, existe o `mvn spring-boot:build-image`, que usa Buildpacks e já aplica boa parte disso."
