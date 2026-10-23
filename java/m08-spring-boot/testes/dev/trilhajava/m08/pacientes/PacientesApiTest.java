package dev.trilhajava.m08.pacientes;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Testes "caixa-preta": só falam HTTP e JSON. Você é livre para organizar as classes como quiser.
 * Cada teste gera CPFs e nomes únicos, então a ordem de execução não importa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Exercício 08.02 — API REST de pacientes")
class PacientesApiTest {

    @Autowired
    MockMvcTester mvc;

    // ------------------------------------------------------------------ auxiliares

    /** Gera um CPF válido aleatório (dígitos verificadores corretos). */
    static String cpfValido() {
        var r = ThreadLocalRandom.current();
        int[] d = new int[11];
        do {
            for (int i = 0; i < 9; i++) {
                d[i] = r.nextInt(10);
            }
        } while (d[0] == d[1] && d[1] == d[2]);
        for (int n = 9; n <= 10; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += d[i] * (n + 1 - i);
            }
            int resto = soma % 11;
            d[n] = resto < 2 ? 0 : 11 - resto;
        }
        var sb = new StringBuilder();
        for (int x : d) {
            sb.append(x);
        }
        return sb.toString();
    }

    static String corpo(String nome, String cpf, String email, String nascimento) {
        return """
                {"nome": %s, "cpf": %s, "email": %s, "nascimento": %s}
                """.formatted(texto(nome), texto(cpf), texto(email), texto(nascimento));
    }

    private static String texto(String valor) {
        return valor == null ? "null" : "\"" + valor + "\"";
    }

    MvcTestResult post(String json) {
        return mvc.post().uri("/pacientes").contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    MvcTestResult put(long id, String json) {
        return mvc.put().uri("/pacientes/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    MvcTestResult get(String uri, Object... variaveis) {
        return mvc.get().uri(uri, variaveis).exchange();
    }

    MvcTestResult delete(long id) {
        return mvc.delete().uri("/pacientes/{id}", id).exchange();
    }

    static DocumentContext json(MvcTestResult resultado) {
        return JsonPath.parse(new String(resultado.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    long criar(String nome, String cpf) {
        var r = post(corpo(nome, cpf, null, "1990-05-10"));
        assertThat(r).as("POST /pacientes deveria criar o paciente").hasStatus(HttpStatus.CREATED);
        return json(r).read("$.id", Long.class);
    }

    // ------------------------------------------------------------------ testes

    @Test
    @DisplayName("POST cria o paciente: 201, Location e corpo com o CPF mascarado")
    void cria() {
        String cpf = cpfValido();
        var r = post(corpo("Ana Souza", cpf, "ana@email.com", "1990-05-10"));

        assertThat(r).hasStatus(HttpStatus.CREATED);
        long id = json(r).read("$.id", Long.class);
        assertThat(r.getResponse().getHeader("Location")).as("header Location").endsWith("/pacientes/" + id);
        assertThat(r).bodyJson().extractingPath("$.nome").isEqualTo("Ana Souza");
        assertThat(r).bodyJson().extractingPath("$.email").isEqualTo("ana@email.com");
        assertThat(r).bodyJson().extractingPath("$.nascimento").isEqualTo("1990-05-10");
        assertThat(r).bodyJson().extractingPath("$.cpf")
                .as("CPF mascarado no formato ***.982.247-**")
                .isEqualTo("***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**");
    }

    @Test
    @DisplayName("GET /pacientes/{id} devolve o paciente cadastrado")
    void buscaPorId() {
        long id = criar("Bruno Lima", cpfValido());
        var r = get("/pacientes/{id}", id);
        assertThat(r).hasStatusOk();
        assertThat(r).bodyJson().extractingPath("$.id").isEqualTo((int) id);
        assertThat(r).bodyJson().extractingPath("$.nome").isEqualTo("Bruno Lima");
    }

    static Stream<Arguments> invalidos() {
        String cpf = cpfValido();
        return Stream.of(
                Arguments.of("nome em branco", corpo("   ", cpf, null, "1990-05-10")),
                Arguments.of("nome ausente", corpo(null, cpf, null, "1990-05-10")),
                Arguments.of("nome com mais de 120 caracteres", corpo("A".repeat(121), cpf, null, "1990-05-10")),
                Arguments.of("CPF com dígito verificador errado", corpo("Ana", "52998224724", null, "1990-05-10")),
                Arguments.of("CPF com máscara", corpo("Ana", "529.982.247-25", null, "1990-05-10")),
                Arguments.of("CPF com todos os dígitos iguais", corpo("Ana", "11111111111", null, "1990-05-10")),
                Arguments.of("CPF ausente", corpo("Ana", null, null, "1990-05-10")),
                Arguments.of("e-mail inválido", corpo("Ana", cpf, "ana@", "1990-05-10")),
                Arguments.of("nascimento no futuro", corpo("Ana", cpf, null, "2999-01-01")),
                Arguments.of("nascimento ausente", corpo("Ana", cpf, null, null)),
                Arguments.of("JSON malformado", "{\"nome\": "));
    }

    @ParameterizedTest(name = "{0} → 400")
    @MethodSource("invalidos")
    @DisplayName("dados inválidos → 400 Bad Request")
    void invalidos(String caso, String json) {
        assertThat(post(json)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("CPF já cadastrado → 409 Conflict")
    void cpfDuplicado() {
        String cpf = cpfValido();
        criar("Carla", cpf);
        assertThat(post(corpo("Outra Carla", cpf, null, "2000-01-01"))).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("DELETE remove (204); depois disso GET e DELETE respondem 404")
    void remove() {
        long id = criar("Davi", cpfValido());
        assertThat(delete(id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(get("/pacientes/{id}", id)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(delete(id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /pacientes filtra por nome (sem diferenciar maiúsculas), ordena por nome e pagina")
    void lista() {
        String marca = "tk" + UUID.randomUUID().toString().substring(0, 8);
        criar("beatriz " + marca, cpfValido());
        criar("Carlos " + marca, cpfValido());
        criar("Alice " + marca, cpfValido());

        var primeira = get("/pacientes?nome={nome}&pagina=0&tamanho=2", marca.toUpperCase());
        assertThat(primeira).hasStatusOk();
        DocumentContext p1 = json(primeira);
        assertThat(p1.read("$.itens[*].nome", List.class)).containsExactly("Alice " + marca, "beatriz " + marca);
        assertThat(p1.read("$.pagina", Integer.class)).isZero();
        assertThat(p1.read("$.tamanho", Integer.class)).isEqualTo(2);
        assertThat(p1.read("$.totalItens", Integer.class)).isEqualTo(3);
        assertThat(p1.read("$.totalPaginas", Integer.class)).isEqualTo(2);

        var segunda = get("/pacientes?nome={nome}&pagina=1&tamanho=2", marca);
        assertThat(json(segunda).read("$.itens[*].nome", List.class)).containsExactly("Carlos " + marca);
    }

    @ParameterizedTest(name = "{0} → 400")
    @ValueSource(strings = {"tamanho=0", "tamanho=51", "pagina=-1"})
    @DisplayName("parâmetros de paginação inválidos → 400")
    void paginacaoInvalida(String parametro) {
        assertThat(get("/pacientes?" + parametro)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("PUT substitui os dados (mantendo o próprio CPF) e responde 200")
    void atualiza() {
        String cpf = cpfValido();
        long id = criar("Elis", cpf);

        var r = put(id, corpo("Elis Regina", cpf, "elis@email.com", "1985-03-17"));
        assertThat(r).hasStatusOk();
        assertThat(r).bodyJson().extractingPath("$.nome").isEqualTo("Elis Regina");

        var depois = get("/pacientes/{id}", id);
        assertThat(depois).bodyJson().extractingPath("$.email").isEqualTo("elis@email.com");
        assertThat(depois).bodyJson().extractingPath("$.nascimento").isEqualTo("1985-03-17");
    }

    @Test
    @DisplayName("PUT: paciente inexistente → 404; CPF de outro paciente → 409; dados inválidos → 400")
    void atualizaComErros() {
        String cpfDaFabia = cpfValido();
        long fabia = criar("Fábia", cpfDaFabia);
        long gil = criar("Gil", cpfValido());

        assertThat(put(gil, corpo("Gil", cpfDaFabia, null, "1990-01-01"))).hasStatus(HttpStatus.CONFLICT);
        assertThat(put(gil, corpo("", cpfValido(), null, "1990-01-01"))).hasStatus(HttpStatus.BAD_REQUEST);

        assertThat(delete(fabia)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(put(fabia, corpo("Fábia", cpfDaFabia, null, "1990-01-01"))).hasStatus(HttpStatus.NOT_FOUND);
    }
}
