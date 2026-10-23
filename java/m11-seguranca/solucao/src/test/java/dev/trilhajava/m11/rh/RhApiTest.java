package dev.trilhajava.m11.rh;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Confere a solução de referência do exercício 11.03 contra os exemplos do enunciado (garante que o que o site
 * mostra funciona). Não faz parte do starter.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Exercício 11.03 — autorização por dono (BOLA)")
class RhApiTest {

    static final String PROBLEMAS = "https://api.rh.dev/problemas/";

    // ids: 1 rita (RH) · 2 gustavo (GESTOR de 3 e 4) · 3 fernanda · 4 felipe · 5 gabriela (GESTOR de 6) · 6 fabio

    @Autowired
    MockMvcTester mvc;

    static String basic(String usuario) {
        String credenciais = usuario + ":senha-de-" + usuario;
        return "Basic " + Base64.getEncoder().encodeToString(credenciais.getBytes(StandardCharsets.UTF_8));
    }

    MvcTestResult get(String uri, String usuario) {
        return mvc.get().uri(uri).header("Authorization", basic(usuario)).exchange();
    }

    MvcTestResult putSalario(long id, String salario, String usuario) {
        return mvc.put().uri("/funcionarios/{id}/salario", id).header("Authorization", basic(usuario))
                .contentType(MediaType.APPLICATION_JSON).content("{\"salario\": " + salario + "}").exchange();
    }

    static DocumentContext json(MvcTestResult r) {
        return JsonPath.parse(new String(r.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    static List<Integer> ids(MvcTestResult r) {
        return json(r).read("$[*].id");
    }

    static void naoEncontrado(MvcTestResult r) {
        assertThat(r).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(json(r).read("$.type", String.class)).isEqualTo(PROBLEMAS + "nao-encontrado");
    }

    static void proibido(MvcTestResult r) {
        assertThat(r).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(json(r).read("$.type", String.class)).isEqualTo(PROBLEMAS + "operacao-nao-permitida");
    }

    @Test
    @DisplayName("sem credenciais → 401")
    void semLogin() {
        assertThat(mvc.get().uri("/funcionarios").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Nested
    @DisplayName("listagem")
    class Listagem {

        @Test
        @DisplayName("RH vê todos, com salário")
        void rh() {
            MvcTestResult r = get("/funcionarios", "rita");
            assertThat(r).hasStatusOk();
            assertThat(ids(r)).containsExactly(1, 2, 3, 4, 5, 6);
            List<Object> salarios = json(r).read("$[*].salario");
            assertThat(salarios).hasSize(6);
        }

        @Test
        @DisplayName("gestor vê a si e aos subordinados diretos — salário só o dele")
        void gestor() {
            MvcTestResult r = get("/funcionarios", "gustavo");
            assertThat(ids(r)).containsExactly(2, 3, 4);
            List<Integer> comSalario = json(r).read("$[?(@.salario)].id");
            assertThat(comSalario).containsExactly(2);
        }

        @Test
        @DisplayName("o gestor não vê a equipe de outro gestor")
        void outroGestor() {
            assertThat(ids(get("/funcionarios", "gabriela"))).containsExactly(5, 6);
        }

        @Test
        @DisplayName("funcionário vê só a si mesmo")
        void funcionario() {
            MvcTestResult r = get("/funcionarios", "fernanda");
            assertThat(ids(r)).containsExactly(3);
            assertThat(json(r).read("$[0].salario", Double.class)).isEqualTo(11000.00);
        }
    }

    @Nested
    @DisplayName("busca por id")
    class Busca {

        @Test
        @DisplayName("o próprio funcionário vê os seus dados, com salário")
        void proprio() {
            MvcTestResult r = get("/funcionarios/4", "felipe");
            assertThat(r).hasStatusOk();
            assertThat(json(r).read("$.nome", String.class)).isEqualTo("Felipe Costa");
            assertThat(json(r).read("$.salario", Double.class)).isEqualTo(8500.00);
        }

        @Test
        @DisplayName("BOLA: um funcionário pedindo o id de um colega recebe 404")
        void colega() {
            naoEncontrado(get("/funcionarios/3", "felipe"));
        }

        @Test
        @DisplayName("o 404 de 'sem permissão' é igual ao de um id que não existe")
        void mesmoCorpo() {
            MvcTestResult semPermissao = get("/funcionarios/3", "felipe");
            MvcTestResult inexistente = get("/funcionarios/999", "felipe");
            naoEncontrado(inexistente);
            assertThat(json(semPermissao).read("$.detail", String.class))
                    .isEqualTo(json(inexistente).read("$.detail", String.class));
        }

        @Test
        @DisplayName("um funcionário não vê o próprio gestor")
        void gestorDoFuncionario() {
            naoEncontrado(get("/funcionarios/2", "fernanda"));
        }

        @Test
        @DisplayName("o gestor vê o subordinado, mas SEM o salário")
        void subordinado() {
            MvcTestResult r = get("/funcionarios/3", "gustavo");
            assertThat(r).hasStatusOk();
            assertThat(json(r).read("$.nome", String.class)).isEqualTo("Fernanda Souza");
            assertThat(r.getResponse().getContentAsByteArray()).asString(StandardCharsets.UTF_8)
                    .doesNotContain("salario").doesNotContain("11000");
        }

        @Test
        @DisplayName("o gestor não vê o subordinado de outro gestor")
        void subordinadoDeOutro() {
            naoEncontrado(get("/funcionarios/6", "gustavo"));
        }
    }

    @Nested
    @DisplayName("alteração de salário")
    class Salario {

        @Test
        @DisplayName("o RH altera o salário de outra pessoa")
        void rh() {
            MvcTestResult r = putSalario(6, "7350.00", "rita");
            assertThat(r).hasStatusOk();
            assertThat(json(r).read("$.salario", Double.class)).isEqualTo(7350.00);
            assertThat(json(get("/funcionarios/6", "fabio")).read("$.salario", Double.class)).isEqualTo(7350.00);
        }

        @Test
        @DisplayName("o RH não altera o próprio salário → 403")
        void rhProprio() {
            proibido(putSalario(1, "50000.00", "rita"));
            assertThat(json(get("/funcionarios/1", "rita")).read("$.salario", Double.class)).isEqualTo(9800.00);
        }

        @Test
        @DisplayName("o gestor VÊ o subordinado, mas não altera o salário → 403")
        void gestor() {
            proibido(putSalario(3, "20000.00", "gustavo"));
        }

        @Test
        @DisplayName("o funcionário não altera o próprio salário → 403")
        void funcionarioProprio() {
            proibido(putSalario(4, "20000.00", "felipe"));
        }

        @Test
        @DisplayName("o funcionário tentando alterar um colega → 404 (nem sabe se ele existe)")
        void funcionarioColega() {
            naoEncontrado(putSalario(3, "1.00", "felipe"));
        }

        @Test
        @DisplayName("o gestor tentando alterar quem não é da equipe dele → 404")
        void gestorDeOutraEquipe() {
            naoEncontrado(putSalario(6, "9000.00", "gustavo"));
        }

        @Test
        @DisplayName("o RH alterando um id que não existe → 404")
        void rhInexistente() {
            naoEncontrado(putSalario(999, "9000.00", "rita"));
        }

        @Test
        @DisplayName("salário zero, negativo ou acima de 1.000.000,00 → 400")
        void invalido() {
            assertThat(putSalario(6, "0", "rita")).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(putSalario(6, "-10.00", "rita")).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(putSalario(6, "1000000.01", "rita")).hasStatus(HttpStatus.BAD_REQUEST);
        }
    }
}
