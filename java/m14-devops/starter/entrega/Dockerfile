# Exercício 14.04 — este Dockerfile funciona, mas não passaria numa revisão.
# Leia o enunciado e reescreva-o: multi-stage, camadas, usuário sem privilégios etc.
FROM eclipse-temurin:latest
COPY target/*.jar app.jar
ENTRYPOINT java -jar app.jar
