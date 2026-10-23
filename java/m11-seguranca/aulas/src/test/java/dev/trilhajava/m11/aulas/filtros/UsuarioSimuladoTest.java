package dev.trilhajava.m11.aulas.filtros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * @WithMockUser e jwt() colocam um Authentication pronto no SecurityContext: a etapa de
 * autenticação é pulada e só as regras de AUTORIZAÇÃO são exercitadas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioSimuladoTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ServicoDeProntuario prontuarios;

    @Test
    @WithMockUser(username = "dra.helena", roles = "MEDICO")
    void medicaLeOProntuario() {
        assertThat(mvc.get().uri("/api/consultas/1/prontuario")).hasStatusOk();
    }

    @Test
    void jwtSimuladoComPapelDeRecepcaoListaConsultas() {
        assertThat(mvc.get().uri("/api/consultas")
                .with(jwt().jwt(token -> token.subject("recepcao"))
                        .authorities(new SimpleGrantedAuthority("ROLE_RECEPCAO"))))
                .hasStatusOk();
    }

    @Test
    void jwtSemOPapelCertoLeva403() {
        assertThat(mvc.get().uri("/api/consultas")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_consultas:ler"))))
                .hasStatus(403);
    }

    @Test
    @WithMockUser(username = "recepcao", roles = "RECEPCAO")
    void aRegraDoMetodoValeMesmoSemPassarPeloHttp() {
        assertThatThrownBy(() -> prontuarios.resumo(1))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("Access Denied");
    }

    @Test
    @WithMockUser(username = "dra.helena", roles = "MEDICO")
    void medicaChamaOServicoDiretamente() {
        assertThat(prontuarios.resumo(1)).startsWith("Consulta 1:");
    }
}
