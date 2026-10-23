package dev.trilhajava.m11.aulas.autorizacao;

import static dev.trilhajava.m11.aulas.autorizacao.Tokens.como;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** A versão corrigida (/prontuarios): os mesmos ataques da v0, agora barrados. */
@SpringBootTest(properties = Tokens.SEGREDO)
@AutoConfigureMockMvc
@DirtiesContext // o cadastro grava no repositório em memória
@DisplayName("API corrigida")
class ProntuarioApiTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    RepositorioDeProntuarios repositorio;

    @Autowired
    RegistroDeNegacoes auditoria;

    @BeforeEach
    void limparAuditoria() {
        auditoria.limpar();
    }

    MvcTestResult get(String uri, String usuario) {
        return mvc.get().uri(uri).with(como(usuario)).exchange();
    }

    @Nested
    @DisplayName("BOLA")
    class Bola {

        @Test
        @DisplayName("a Ana lê o próprio prontuário, com anotações")
        void donaLe() {
            assertThat(get("/prontuarios/1001", "pac-ana")).hasStatusOk()
                    .bodyJson().extractingPath("$.anotacoes").isEqualTo("Suspeita de enxaqueca; pedir ressonância.");
        }

        @Test
        @DisplayName("o Bruno recebe 404 no prontuário da Ana, com o MESMO corpo de um id que não existe")
        void outroPacienteRecebe404Identico() {
            MvcTestResult daAna = get("/prontuarios/1001", "pac-bruno");
            MvcTestResult inexistente = get("/prontuarios/9999", "pac-bruno");

            String corpo = """
                    {"type": "https://api.clinicas.dev/problemas/nao-encontrado", "title": "Not Found",
                     "status": 404, "detail": "Prontuário não encontrado"}""";
            assertThat(daAna).hasStatus(HttpStatus.NOT_FOUND).bodyJson().isLenientlyEqualTo(corpo);
            assertThat(inexistente).hasStatus(HttpStatus.NOT_FOUND).bodyJson().isLenientlyEqualTo(corpo);
            // só "instance" difere: é o caminho que o próprio cliente pediu, não revela nada
        }

        @Test
        @DisplayName("médico de outra clínica recebe 404, e a negação vai para a auditoria")
        void outroTenantRecebe404EAuditado() {
            assertThat(get("/prontuarios/1002", "med-heitor")).hasStatus(HttpStatus.NOT_FOUND);
            assertThat(auditoria.recentes()).containsExactly(
                    "negado usuario=med-heitor clinica=lua operacao=ler recurso=prontuario/1002 resultado=404");
        }

        @Test
        @DisplayName("a recepção vê o resumo, sem as anotações clínicas")
        void recepcaoVeResumo() {
            MvcTestResult resposta = get("/prontuarios/1001", "atd-elisa");
            assertThat(resposta).hasStatusOk();
            assertThat(resposta).bodyJson().isStrictlyEqualTo("""
                    {"id": 1001, "pacienteId": "pac-ana", "medicoId": "med-carla",
                     "queixa": "Dor de cabeça recorrente"}""");
        }

        @Test
        @DisplayName("a listagem do paciente traz só os prontuários dele")
        void listagemEscopada() {
            assertThat(get("/prontuarios", "pac-bruno")).hasStatusOk()
                    .bodyJson().extractingPath("$[*].id").asArray().containsExactly(1002);
        }
    }

    @Nested
    @DisplayName("mass assignment")
    class MassAssignment {

        @Test
        @DisplayName("clinicaId, id e anotacoes no corpo são ignorados: a clínica vem do token")
        void camposProibidosIgnorados() {
            MvcTestResult resposta = mvc.post().uri("/prontuarios").with(como("atd-iara"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"id": 1002, "clinicaId": "sol", "pacienteId": "pac-julia", "medicoId": "med-heitor",
                             "queixa": "Retorno", "anotacoes": "Alta definitiva."}""")
                    .exchange();

            assertThat(resposta).hasStatus(HttpStatus.CREATED);
            assertThat(resposta).bodyJson().isStrictlyEqualTo("""
                    {"id": 1004, "pacienteId": "pac-julia", "medicoId": "med-heitor", "queixa": "Retorno"}""");
            assertThat(repositorio.porId(1004).orElseThrow().clinicaId()).isEqualTo("lua");
            assertThat(repositorio.porId(1002).orElseThrow().anotacoes()).isEqualTo("LDL 190 mg/dL; iniciar estatina.");
        }

        @Test
        @DisplayName("médico de outra clínica no corpo: 422")
        void medicoDeOutraClinica() {
            MvcTestResult resposta = mvc.post().uri("/prontuarios").with(como("atd-iara"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"pacienteId": "pac-julia", "medicoId": "med-carla", "queixa": "Retorno"}""")
                    .exchange();

            assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        }

        @Test
        @DisplayName("BFLA: paciente não cadastra prontuário (403 do @PreAuthorize)")
        void pacienteNaoCadastra() {
            MvcTestResult resposta = mvc.post().uri("/prontuarios").with(como("pac-ana"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"pacienteId": "pac-ana", "medicoId": "med-carla", "queixa": "Retorno"}""")
                    .exchange();

            assertThat(resposta).hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("method security com bean e AuthorizationManager de rota")
    class MetodoERota {

        @Test
        @DisplayName("exames: a dona vê; outro paciente recebe 404 (o handler troca o 403)")
        void examesComBeanNoSpel() {
            assertThat(get("/prontuarios/1001/exames", "pac-ana")).hasStatusOk()
                    .bodyJson().isStrictlyEqualTo("""
                            ["Ressonância de crânio: sem alterações"]""");
            assertThat(get("/prontuarios/1001/exames", "pac-bruno")).hasStatus(HttpStatus.NOT_FOUND);
            assertThat(get("/prontuarios/9999/exames", "pac-bruno")).hasStatus(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("relatório: a recepção da própria clínica vê")
        void relatorioDaPropriaClinica() {
            assertThat(get("/clinicas/sol/relatorios/atendimentos-por-medico", "atd-elisa")).hasStatusOk()
                    .bodyJson().isStrictlyEqualTo("""
                            {"med-carla": 1, "med-diego": 1}""");
        }

        @Test
        @DisplayName("relatório de outra clínica: 403, e o Spring Security publica o evento de negação")
        void relatorioDeOutraClinica() {
            assertThat(get("/clinicas/lua/relatorios/atendimentos-por-medico", "atd-elisa"))
                    .hasStatus(HttpStatus.FORBIDDEN);
            assertThat(auditoria.recentes()).containsExactly("negado usuario=atd-elisa clinica=? "
                    + "operacao=spring-security recurso=rota/clinicas/lua/relatorios/atendimentos-por-medico "
                    + "resultado=NEGADO");
        }

        @Test
        @DisplayName("relatório: médico da própria clínica não tem o papel, 403")
        void relatorioSemPapel() {
            assertThat(get("/clinicas/sol/relatorios/atendimentos-por-medico", "med-carla"))
                    .hasStatus(HttpStatus.FORBIDDEN);
        }
    }
}
