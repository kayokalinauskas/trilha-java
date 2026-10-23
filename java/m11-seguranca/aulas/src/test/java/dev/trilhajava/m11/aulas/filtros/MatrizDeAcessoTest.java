package dev.trilhajava.m11.aulas.filtros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;

/**
 * A matriz de acesso da API: cada linha é um perfil tentando uma operação.
 * As credenciais passam de verdade pelo BasicAuthenticationFilter, pelo
 * UserDetailsService e pelo PasswordEncoder: nada é simulado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MatrizDeAcessoTest {

    static final Map<String, String> SENHAS = Map.of(
            "recepcao", "recepcao-123",
            "dra.helena", "medica-123",
            "admin", "admin-123");

    static final String CONSULTA = """
            {"paciente": "João Pereira", "medico": "dra.helena", "horario": "2030-03-10T14:30:00"}""";

    @Autowired
    MockMvcTester mvc;

    @ParameterizedTest(name = "{2} {0} {1} -> {3}")
    @CsvSource(textBlock = """
            GET,  /api/status,                  anonimo,    200
            GET,  /api/consultas,               anonimo,    401
            GET,  /api/consultas,               recepcao,   200
            GET,  /api/consultas,               dra.helena, 200
            GET,  /api/consultas,               admin,      403
            POST, /api/consultas,               anonimo,    401
            POST, /api/consultas,               recepcao,   201
            POST, /api/consultas,               dra.helena, 403
            POST, /api/consultas,               admin,      403
            GET,  /api/consultas/1/prontuario,  anonimo,    401
            GET,  /api/consultas/1/prontuario,  recepcao,   403
            GET,  /api/consultas/1/prontuario,  dra.helena, 200
            GET,  /api/consultas/1/prontuario,  admin,      403
            GET,  /api/relatorios,              recepcao,   403
            """)
    void matriz(String metodo, String caminho, String usuario, int statusEsperado) {
        MockMvcRequestBuilder requisicao = mvc.method(HttpMethod.valueOf(metodo)).uri(caminho);
        if (metodo.equals("POST")) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(CONSULTA);
        }
        if (!usuario.equals("anonimo")) {
            requisicao.with(httpBasic(usuario, SENHAS.get(usuario)));
        }

        assertThat(requisicao.exchange()).hasStatus(statusEsperado);
    }
}
