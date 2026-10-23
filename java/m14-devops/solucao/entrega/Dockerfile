# Exercício 14.04 — imagem de produção de uma aplicação Spring Boot.
# Pré-requisito: o jar executável já foi gerado (mvn -B verify) em target/.
# Build: docker build -f entrega/Dockerfile -t trilha-m14:dev .

# Estágio 1: extrai o jar em camadas. Dependências mudam pouco; o código muda sempre.
# Separando as duas, o docker reaproveita a camada de dependências (dezenas de MB) entre builds.
FROM eclipse-temurin:25-jre-noble AS extrator
WORKDIR /build
ARG JAR=target/*.jar
COPY ${JAR} aplicacao.jar
RUN java -Djarmode=tools -jar aplicacao.jar extract --layers --destination extraido

# Estágio 2: só o JRE (sem JDK, sem Maven, sem código-fonte).
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
# Usuário sem privilégios: se alguém explorar a aplicação, não vira root no container.
RUN groupadd --system app && useradd --system --gid app --uid 1001 app
# Da camada que menos muda para a que mais muda.
COPY --from=extrator /build/extraido/dependencies/ ./
COPY --from=extrator /build/extraido/spring-boot-loader/ ./
COPY --from=extrator /build/extraido/snapshot-dependencies/ ./
COPY --from=extrator /build/extraido/application/ ./
USER app
EXPOSE 8080
# A JVM respeita o limite de memória do container; 75% para o heap, o resto para metaspace, threads etc.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
# Formato exec (JSON): o java é o PID 1 e recebe o SIGTERM do docker stop (desligamento gracioso).
ENTRYPOINT ["java", "-jar", "aplicacao.jar"]
