# Fintech Core — imagem de produção (marco 6).
# 1) mvn -B verify          -> gera target/fintech-core.jar (jar executável)
# 2) docker build -t fintech-core:dev .

# Estágio 1: separa o jar em camadas. As dependências quase nunca mudam; o código muda a cada commit.
FROM eclipse-temurin:25-jre-noble AS extrator
WORKDIR /build
COPY target/fintech-core.jar fintech-core.jar
RUN java -Djarmode=tools -jar fintech-core.jar extract --layers --destination extraido

# Estágio 2: só o JRE e a aplicação.
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
RUN groupadd --system fintech && useradd --system --gid fintech --uid 1001 fintech
# Da camada que menos muda para a que mais muda: um commit só reconstrói a última.
COPY --from=extrator /build/extraido/dependencies/ ./
COPY --from=extrator /build/extraido/spring-boot-loader/ ./
COPY --from=extrator /build/extraido/snapshot-dependencies/ ./
COPY --from=extrator /build/extraido/application/ ./
USER fintech
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
# Formato exec: o java é o PID 1 e recebe o SIGTERM; o Spring termina as requisições em andamento.
ENTRYPOINT ["java", "-jar", "fintech-core.jar"]
