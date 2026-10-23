package dev.trilhajava.m13.aulas.modular;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import dev.trilhajava.m13.aulas.modular.agenda.ServicoDeAgenda;
import dev.trilhajava.m13.aulas.modular.agenda.eventos.ConsultaRealizada;
import dev.trilhajava.m13.aulas.modular.cobranca.Cobrancas;
import dev.trilhajava.m13.aulas.modular.cobranca.OperadoraDeConvenio;
import dev.trilhajava.m13.aulas.modular.notificacoes.AvisosAoPaciente;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** A aplicação inteira: a operadora cai, a publicação da cobrança fica pendente e é reenviada. */
@SpringBootTest(classes = ClinicaApplication.class)
class RegistroDePublicacoesTest {

    @Autowired ServicoDeAgenda agenda;
    @Autowired Cobrancas cobrancas;
    @Autowired AvisosAoPaciente avisos;
    @Autowired IncompleteEventPublications incompletas;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean OperadoraDeConvenio operadora;

    @Test
    void operadoraForaDoArNaoPerdeACobranca() {
        given(operadora.autorizar(any(), any()))
                .willThrow(new IllegalStateException("operadora indisponível (HTTP 503)"))
                .willReturn("AUT-77120");
        UUID consulta = agenda.agendar("Marina Couto", "retorno de cardiologia", new BigDecimal("180.00"));

        agenda.registrarAtendimento(consulta);

        // O atendimento foi registrado e o aviso saiu, mesmo com a operadora fora do ar.
        assertThat(agenda.realizada(consulta)).isTrue();
        await().atMost(Duration.ofSeconds(5)).until(() -> avisos.enviados().size() == 1);
        await().atMost(Duration.ofSeconds(5)).until(() -> statusDa("GeracaoDeCobranca").equals("FAILED"));
        imprimirRegistro("depois da falha");
        assertThat(cobrancas.daConsulta(consulta)).isEmpty();

        // A operadora voltou: reenviamos só as publicações desta consulta que ficaram pendentes.
        incompletas.resubmitIncompletePublications(publicacao ->
                publicacao.getEvent() instanceof ConsultaRealizada evento && evento.consulta().equals(consulta));

        await().atMost(Duration.ofSeconds(5)).until(() -> cobrancas.daConsulta(consulta).isPresent());
        await().atMost(Duration.ofSeconds(5)).until(() -> statusDa("GeracaoDeCobranca").equals("COMPLETED"));
        imprimirRegistro("depois do reenvio");
        assertThat(cobrancas.daConsulta(consulta).orElseThrow().autorizacao()).isEqualTo("AUT-77120");
        assertThat(avisos.enviados()).hasSize(1);   // o ouvinte que deu certo não rodou de novo
    }

    String statusDa(String ouvinte) {
        return jdbc.queryForObject(
                "select status from event_publication where listener_id like ?", String.class, "%" + ouvinte + "%");
    }

    void imprimirRegistro(String momento) {
        System.out.println("-- event_publication " + momento);
        jdbc.query("select listener_id, status, completion_attempts, completion_date from event_publication",
                linha -> {
                    // listener_id = pacote.Classe.metodo(pacote.Evento); mostramos só Classe.metodo
                    String ouvinte = linha.getString("listener_id");
                    String metodo = ouvinte.substring(0, ouvinte.indexOf('('));
                    String classe = metodo.substring(0, metodo.lastIndexOf('.'));
                    System.out.printf("%-35s %-10s %d %s%n",
                            metodo.substring(classe.lastIndexOf('.') + 1),
                            linha.getString("status"), linha.getInt("completion_attempts"),
                            linha.getTimestamp("completion_date") == null ? "(pendente)" : "concluída");
                });
    }
}
