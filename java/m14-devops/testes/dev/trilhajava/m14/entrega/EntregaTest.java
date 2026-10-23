package dev.trilhajava.m14.entrega;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Checagens estáticas dos arquivos de entrega (não roda docker nem GitHub Actions):
 * lê entrega/Dockerfile, entrega/Dockerfile.dockerignore, entrega/compose.yaml e entrega/ci.yml.
 */
@DisplayName("Exercício 14.04 — Dockerfile, Compose e pipeline de CI")
class EntregaTest {

    static final Path ENTREGA = Path.of("entrega");

    static String ler(String arquivo) {
        try {
            return Files.readString(ENTREGA.resolve(arquivo));
        } catch (IOException e) {
            throw new AssertionError("arquivo entrega/" + arquivo + " não encontrado", e);
        }
    }

    /** Instruções do Dockerfile, sem comentários e linhas vazias, com as continuações (\) juntadas. */
    static List<String> instrucoes() {
        String semContinuacao = ler("Dockerfile").replaceAll("\\\\\\r?\\n", " ");
        return semContinuacao.lines().map(String::strip)
                .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                .toList();
    }

    static List<String> comecandoCom(String comando) {
        return instrucoes().stream().filter(l -> l.toUpperCase().startsWith(comando + " ")).toList();
    }

    /** Instruções a partir do último FROM (a imagem que vai para produção). */
    static List<String> estagioFinal() {
        List<String> todas = instrucoes();
        int ultimoFrom = 0;
        for (int i = 0; i < todas.size(); i++) {
            if (todas.get(i).toUpperCase().startsWith("FROM ")) {
                ultimoFrom = i;
            }
        }
        return todas.subList(ultimoFrom, todas.size());
    }

    @SuppressWarnings("unchecked")
    static Map<Object, Object> yaml(String arquivo) {
        return (Map<Object, Object>) new Yaml().load(ler(arquivo));
    }

    @SuppressWarnings("unchecked")
    static Map<Object, Object> mapa(Object o) {
        assertThat(o).isInstanceOf(Map.class);
        return (Map<Object, Object>) o;
    }

    @Nested
    @DisplayName("Dockerfile")
    class Dockerfile {

        @Test
        @DisplayName("é multi-stage: pelo menos dois FROM, o primeiro com nome (AS)")
        void multiStage() {
            List<String> froms = comecandoCom("FROM");
            assertThat(froms).hasSizeGreaterThanOrEqualTo(2);
            assertThat(froms.getFirst()).containsPattern("(?i)\\sAS\\s+\\w+");
        }

        @Test
        @DisplayName("toda imagem base tem tag explícita, e nunca latest")
        void tagsFixas() {
            Pattern from = Pattern.compile("(?i)^FROM\\s+(?:--platform=\\S+\\s+)?(\\S+)");
            for (String linha : comecandoCom("FROM")) {
                Matcher m = from.matcher(linha);
                assertThat(m.find()).isTrue();
                String imagem = m.group(1);
                assertThat(imagem).as(linha).contains(":");
                assertThat(imagem).as(linha).doesNotEndWith(":latest");
            }
        }

        @Test
        @DisplayName("a imagem final é só um JRE (sem JDK, sem Maven)")
        void imagemFinalJre() {
            String from = estagioFinal().getFirst().toLowerCase();
            assertThat(from).contains("jre").doesNotContain("jdk").doesNotContain("maven");
        }

        @Test
        @DisplayName("extrai o jar em camadas com -Djarmode=tools e copia dependências antes do código")
        void camadas() {
            String tudo = String.join("\n", instrucoes());
            assertThat(tudo).contains("-Djarmode=tools").contains("extract").contains("--layers");
            int dependencias = tudo.indexOf("/dependencies");
            int aplicacao = tudo.indexOf("/application");
            assertThat(dependencias).as("copia a camada dependencies").isPositive();
            assertThat(aplicacao).as("copia a camada application").isPositive();
            assertThat(dependencias).as("dependências antes do código (cache)").isLessThan(aplicacao);
        }

        @Test
        @DisplayName("roda com usuário sem privilégios (USER que não é root)")
        void naoRoot() {
            List<String> users = estagioFinal().stream().filter(l -> l.toUpperCase().startsWith("USER ")).toList();
            assertThat(users).isNotEmpty();
            String usuario = users.getLast().substring(5).strip();
            assertThat(usuario).isNotIn("root", "0", "0:0");
        }

        @Test
        @DisplayName("EXPOSE 8080, ENTRYPOINT em formato exec e heap proporcional à memória do container")
        void execucao() {
            assertThat(comecandoCom("EXPOSE")).anyMatch(l -> l.contains("8080"));
            List<String> entrypoints = comecandoCom("ENTRYPOINT");
            assertThat(entrypoints).isNotEmpty();
            assertThat(entrypoints.getLast().substring("ENTRYPOINT".length()).strip()).startsWith("[");
            assertThat(String.join("\n", estagioFinal())).contains("MaxRAMPercentage");
        }

        @Test
        @DisplayName("o Dockerfile.dockerignore deixa fora .git e .env, mas não o jar")
        void dockerignore() {
            List<String> linhas = ler("Dockerfile.dockerignore").lines().map(String::strip)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
            assertThat(linhas).contains(".git", ".env");
            assertThat(linhas).as("ignorar target/ inteiro tira o jar do contexto")
                    .doesNotContain("target", "target/", "target/*", "*.jar");
        }
    }

    @Nested
    @DisplayName("compose.yaml")
    class Compose {

        Map<Object, Object> servicos() {
            return mapa(yaml("compose.yaml").get("services"));
        }

        @Test
        @DisplayName("tem os serviços app e postgres; o Postgres com tag fixa")
        void temAppEPostgres() {
            assertThat(servicos()).containsKeys("app", "postgres");
            String imagem = String.valueOf(mapa(servicos().get("postgres")).get("image"));
            assertThat(imagem).contains(":").doesNotEndWith(":latest");
        }

        @Test
        @DisplayName("o Postgres tem healthcheck com pg_isready")
        void healthcheck() {
            Map<Object, Object> healthcheck = mapa(mapa(servicos().get("postgres")).get("healthcheck"));
            assertThat(String.valueOf(healthcheck.get("test"))).contains("pg_isready");
        }

        @Test
        @DisplayName("a app é construída pelo Dockerfile e só sobe com o Postgres saudável (service_healthy)")
        void dependeDoBancoSaudavel() {
            Map<Object, Object> app = mapa(servicos().get("app"));
            Map<Object, Object> dependencia = mapa(mapa(app.get("depends_on")).get("postgres"));
            assertThat(dependencia.get("condition")).isEqualTo("service_healthy");
            assertThat(app).containsKey("build");
        }

        @Test
        @DisplayName("nenhuma senha escrita no arquivo: vêm do ambiente (${...})")
        void senhaDoAmbiente() {
            for (Object servico : servicos().values()) {
                Object ambiente = mapa(servico).get("environment");
                if (ambiente instanceof Map<?, ?> vars) {
                    vars.forEach((nome, valor) -> {
                        if (String.valueOf(nome).contains("PASSWORD")) {
                            assertThat(String.valueOf(valor)).as(String.valueOf(nome)).startsWith("${");
                        }
                    });
                }
            }
        }
    }

    @Nested
    @DisplayName("ci.yml (GitHub Actions)")
    class Pipeline {

        @Test
        @DisplayName("roda em push e em pull_request")
        void gatilhos() {
            Map<Object, Object> ci = yaml("ci.yml");
            // YAML 1.1: a chave `on` sem aspas é lida como o booleano true.
            Object on = ci.containsKey("on") ? ci.get("on") : ci.get(Boolean.TRUE);
            assertThat(mapa(on)).containsKeys("push", "pull_request");
        }

        @SuppressWarnings("unchecked")
        List<Map<Object, Object>> passos() {
            Map<Object, Object> jobs = mapa(yaml("ci.yml").get("jobs"));
            assertThat(jobs).isNotEmpty();
            return (List<Map<Object, Object>>) mapa(jobs.values().iterator().next()).get("steps");
        }

        @Test
        @DisplayName("configura o Java 25 (Temurin) com cache do Maven")
        void setupJava() {
            Map<Object, Object> setup = passos().stream()
                    .filter(p -> String.valueOf(p.get("uses")).startsWith("actions/setup-java@"))
                    .findFirst().orElseThrow(() -> new AssertionError("falta o passo actions/setup-java"));
            Map<Object, Object> com = mapa(setup.get("with"));
            assertThat(String.valueOf(com.get("java-version"))).isEqualTo("25");
            assertThat(com.get("distribution")).isEqualTo("temurin");
            assertThat(com.get("cache")).isEqualTo("maven");
        }

        @Test
        @DisplayName("faz checkout antes de tudo e roda mvn -B verify")
        void build() {
            List<Map<Object, Object>> passos = passos();
            assertThat(String.valueOf(passos.getFirst().get("uses"))).startsWith("actions/checkout@");
            assertThat(passos).anyMatch(p -> {
                String run = String.valueOf(p.get("run"));
                return run.contains("mvn") && run.contains("-B") && run.contains("verify");
            });
        }
    }
}
