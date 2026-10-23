package dev.trilhajava.m11.aulas.filtros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** O filtro de auditoria vê o usuário autenticado E o status final, inclusive 401 e 403. */
@SpringBootTest(properties = "logging.level.dev.trilhajava.m11.aulas.filtros=INFO")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class AuditoriaTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void registraQuemFoiBarradoEComQueStatus(CapturedOutput saida) {
        mvc.get().uri("/api/consultas").with(httpBasic("admin", "admin-123")).exchange();

        assertThat(saida).contains("auditoria usuario=admin metodo=GET caminho=/api/consultas status=403");
    }

    @Test
    void registraOAcessoPermitido(CapturedOutput saida) {
        mvc.get().uri("/api/consultas").with(httpBasic("dra.helena", "medica-123")).exchange();

        assertThat(saida).contains("auditoria usuario=dra.helena metodo=GET caminho=/api/consultas status=200");
    }

    @Test
    void registraUmaVezSo(CapturedOutput saida) {
        mvc.get().uri("/api/status").exchange();

        assertThat(saida.getOut().split("caminho=/api/status", -1)).hasSize(2);
    }
}
