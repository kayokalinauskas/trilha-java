package dev.trilhajava.fintech;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Marco 6: checagens estáticas do Dockerfile, do .dockerignore e do compose.producao.yaml (na raiz do projeto).
 * Não constroem a imagem: para isso, siga o roteiro do enunciado (mvn -B verify, docker build, docker compose up).
 */
@DisplayName("Fintech Core — Marco 6: Dockerfile e Compose de produção")
class Marco6EntregaTest {

    static String ler(String arquivo) {
        try {
            return Files.readString(Path.of(arquivo));
        } catch (IOException e) {
            throw new AssertionError("arquivo " + arquivo + " não encontrado na raiz do projeto", e);
        }
    }

    static List<String> instrucoes() {
        return ler("Dockerfile").replaceAll("\\\\\\r?\\n", " ").lines().map(String::strip)
                .filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
    }

    static List<String> comecandoCom(String comando) {
        return instrucoes().stream().filter(l -> l.toUpperCase().startsWith(comando + " ")).toList();
    }

    @Test
    @DisplayName("Dockerfile multi-stage, com imagens de tag fixa (nunca latest) e a final só com JRE")
    void imagens() {
        List<String> froms = comecandoCom("FROM");
        assertThat(froms).hasSizeGreaterThanOrEqualTo(2);
        assertThat(froms.getFirst()).containsPattern("(?i)\\sAS\\s+\\w+");
        for (String from : froms) {
            String imagem = from.split("\\s+")[1];
            assertThat(imagem).as(from).contains(":").doesNotEndWith(":latest");
        }
        assertThat(froms.getLast().toLowerCase()).contains("jre").doesNotContain("jdk");
    }

    @Test
    @DisplayName("usa o jar executável do Maven, extraído em camadas (dependências antes do código)")
    void camadas() {
        String tudo = String.join("\n", instrucoes());
        assertThat(tudo).contains("target/").contains(".jar");
        assertThat(tudo).contains("-Djarmode=tools").contains("--layers");
        assertThat(tudo.indexOf("/dependencies")).isPositive().isLessThan(tudo.indexOf("/application"));
    }

    @Test
    @DisplayName("roda sem root, expõe 8080, limita o heap pela memória do container e usa ENTRYPOINT exec")
    void execucao() {
        List<String> users = comecandoCom("USER");
        assertThat(users).isNotEmpty();
        assertThat(users.getLast().substring(5).strip()).isNotIn("root", "0", "0:0");
        assertThat(comecandoCom("EXPOSE")).anyMatch(l -> l.contains("8080"));
        assertThat(String.join("\n", instrucoes())).contains("MaxRAMPercentage");
        assertThat(comecandoCom("ENTRYPOINT").getLast().substring("ENTRYPOINT".length()).strip()).startsWith("[");
    }

    @Test
    @DisplayName(".dockerignore deixa de fora .git e .env, mas não o jar")
    void dockerignore() {
        List<String> linhas = ler(".dockerignore").lines().map(String::strip)
                .filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
        assertThat(linhas).contains(".git", ".env").doesNotContain("target", "target/", "target/*", "*.jar");
    }

    @Test
    @DisplayName("compose.producao.yaml: app, postgres e kafka; app espera o banco saudável; senhas do ambiente")
    void compose() {
        String compose = ler("compose.producao.yaml");
        assertThat(compose).contains("app:").contains("postgres:").contains("kafka:");
        assertThat(compose).contains("service_healthy").contains("pg_isready");
        assertThat(compose).containsPattern("POSTGRES_PASSWORD: \\$\\{");
        assertThat(compose).containsPattern("SPRING_DATASOURCE_PASSWORD: \\$\\{");
        assertThat(compose).containsPattern("FINTECH_JWT_SEGREDO: \\$\\{");
        assertThat(compose).doesNotContain(":latest");
    }
}
