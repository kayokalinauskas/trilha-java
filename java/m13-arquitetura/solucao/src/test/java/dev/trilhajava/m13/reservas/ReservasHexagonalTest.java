package dev.trilhajava.m13.reservas;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jayway.jsonpath.JsonPath;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import dev.trilhajava.m13.reservas.aplicacao.ServicoDeReservas;
import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.ReservarSala.PedidoDeReserva;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.CatalogoDeSalas;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.Notificador;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.RepositorioDeReservas;
import dev.trilhajava.m13.reservas.dominio.ConflitoDeHorarioException;
import dev.trilhajava.m13.reservas.dominio.NaoEncontradoException;
import dev.trilhajava.m13.reservas.dominio.OperacaoNaoPermitidaException;
import dev.trilhajava.m13.reservas.dominio.Reserva;
import dev.trilhajava.m13.reservas.dominio.ReservaInvalidaException;
import dev.trilhajava.m13.reservas.dominio.Sala;
import dev.trilhajava.m13.reservas.dominio.StatusDaReserva;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@DisplayName("Exercício 13.02 — reserva de salas em arquitetura hexagonal")
class ReservasHexagonalTest {

    /** "Agora" nos testes: quinta, 1º de outubro de 2026, 9h em São Paulo. */
    static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), SP);

    static LocalDateTime em(String dataHora) {
        return LocalDateTime.parse(dataHora);
    }

    // ------------------------------------------------------------------ fakes das portas de saída

    static final class ReservasEmMemoria implements RepositorioDeReservas {
        final Map<UUID, Reserva> dados = new HashMap<>();

        @Override public void salvar(Reserva r) { dados.put(r.id(), r); }
        @Override public Optional<Reserva> buscar(UUID id) { return Optional.ofNullable(dados.get(id)); }
        @Override public List<Reserva> daSalaNoDia(String sala, LocalDate dia) {
            return dados.values().stream()
                    .filter(r -> r.sala().equals(sala) && r.periodo().inicio().toLocalDate().equals(dia)).toList();
        }
    }

    static final class NotificadorGravador implements Notificador {
        final List<String> avisos = new ArrayList<>();

        @Override public void reservaConfirmada(Reserva r) { avisos.add("confirmada " + r.id()); }
        @Override public void reservaCancelada(Reserva r) { avisos.add("cancelada " + r.id()); }
    }

    @Nested
    @DisplayName("o núcleo, testado com fakes (sem Spring)")
    class Nucleo {

        final ReservasEmMemoria reservas = new ReservasEmMemoria();
        final NotificadorGravador notificador = new NotificadorGravador();
        final CatalogoDeSalas salas = id -> switch (id) {
            case "aquario" -> Optional.of(new Sala("aquario", "Aquário", 6));
            case "foco" -> Optional.of(new Sala("foco", "Foco", 2));
            default -> Optional.empty();
        };
        final ServicoDeReservas servico = new ServicoDeReservas(reservas, salas, notificador, RELOGIO);

        Reserva reservar(String sala, String quem, int pessoas, String inicio, String fim) {
            return servico.reservar(new PedidoDeReserva(sala, quem, pessoas, em(inicio), em(fim)));
        }

        @Test
        @DisplayName("reserva válida: ATIVA, salva e confirmada")
        void valida() {
            Reserva r = reservar("aquario", "ana", 4, "2026-10-02T10:00", "2026-10-02T11:30");

            assertThat(r.status()).isEqualTo(StatusDaReserva.ATIVA);
            assertThat(reservas.buscar(r.id())).isPresent();
            assertThat(notificador.avisos).containsExactly("confirmada " + r.id());
        }

        @Test
        @DisplayName("horário sobreposto na mesma sala → conflito; encostado (fim = início) pode")
        void conflito() {
            reservar("aquario", "ana", 4, "2026-10-02T10:00", "2026-10-02T11:00");

            assertThatThrownBy(() -> reservar("aquario", "bia", 2, "2026-10-02T10:45", "2026-10-02T11:15"))
                    .isInstanceOf(ConflitoDeHorarioException.class);
            assertThatThrownBy(() -> reservar("aquario", "bia", 2, "2026-10-02T09:00", "2026-10-02T12:00"))
                    .isInstanceOf(ConflitoDeHorarioException.class);
            assertThat(reservar("aquario", "bia", 2, "2026-10-02T11:00", "2026-10-02T11:30").status())
                    .isEqualTo(StatusDaReserva.ATIVA);
            assertThat(reservar("foco", "caio", 1, "2026-10-02T10:00", "2026-10-02T11:00").status())
                    .isEqualTo(StatusDaReserva.ATIVA);
        }

        @Test
        @DisplayName("regras de horário: das 8h às 20h, no mesmo dia, em múltiplos de 15 min, no futuro")
        void horario() {
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-02T07:45", "2026-10-02T08:30"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-02T19:30", "2026-10-02T20:15"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-02T10:10", "2026-10-02T11:00"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-01T08:00", "2026-10-01T08:45"))
                    .as("no passado").isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-02T19:00", "2026-10-03T09:00"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThat(reservar("aquario", "ana", 2, "2026-10-02T19:00", "2026-10-02T20:00").status())
                    .isEqualTo(StatusDaReserva.ATIVA);
        }

        @Test
        @DisplayName("duração de 30 min a 4 h; participantes de 1 até a capacidade")
        void duracaoECapacidade() {
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-02T10:00", "2026-10-02T10:15"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("aquario", "ana", 2, "2026-10-02T10:00", "2026-10-02T14:15"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("foco", "ana", 3, "2026-10-02T10:00", "2026-10-02T11:00"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(() -> reservar("foco", "ana", 0, "2026-10-02T10:00", "2026-10-02T11:00"))
                    .isInstanceOf(ReservaInvalidaException.class);
            assertThat(reservar("aquario", "ana", 6, "2026-10-02T10:00", "2026-10-02T14:00").status())
                    .isEqualTo(StatusDaReserva.ATIVA);
        }

        @Test
        @DisplayName("sala inexistente → não encontrada")
        void salaInexistente() {
            assertThatThrownBy(() -> reservar("cozinha", "ana", 2, "2026-10-02T10:00", "2026-10-02T11:00"))
                    .isInstanceOf(NaoEncontradoException.class);
        }

        @Test
        @DisplayName("cancelar: só o responsável, só antes do início; libera o horário e avisa uma vez")
        void cancelar() {
            Reserva r = reservar("aquario", "ana", 4, "2026-10-02T10:00", "2026-10-02T11:00");

            assertThatThrownBy(() -> servico.cancelar(r.id(), "bia")).isInstanceOf(OperacaoNaoPermitidaException.class);
            servico.cancelar(r.id(), "ana");
            servico.cancelar(r.id(), "ana");   // de novo: nada muda

            assertThat(reservas.buscar(r.id()).orElseThrow().status()).isEqualTo(StatusDaReserva.CANCELADA);
            assertThat(notificador.avisos).containsExactly("confirmada " + r.id(), "cancelada " + r.id());
            assertThat(reservar("aquario", "bia", 2, "2026-10-02T10:00", "2026-10-02T11:00").status())
                    .isEqualTo(StatusDaReserva.ATIVA);
            assertThatThrownBy(() -> servico.cancelar(UUID.randomUUID(), "ana")).isInstanceOf(NaoEncontradoException.class);
        }

        @Test
        @DisplayName("cancelar depois do início não pode")
        void cancelarDepoisDoInicio() {
            Reserva comecou = new Reserva(UUID.randomUUID(), "aquario", "ana", 2,
                    new dev.trilhajava.m13.reservas.dominio.Periodo(em("2026-10-01T08:30"), em("2026-10-01T09:30")),
                    StatusDaReserva.ATIVA);
            reservas.salvar(comecou);

            assertThatThrownBy(() -> servico.cancelar(comecou.id(), "ana")).isInstanceOf(ReservaInvalidaException.class);
        }

        @Test
        @DisplayName("agenda: só as ativas daquela sala e daquele dia, em ordem de início")
        void agenda() {
            Reserva tarde = reservar("aquario", "ana", 2, "2026-10-02T15:00", "2026-10-02T16:00");
            Reserva manha = reservar("aquario", "bia", 2, "2026-10-02T09:00", "2026-10-02T10:00");
            Reserva cancelada = reservar("aquario", "caio", 2, "2026-10-02T11:00", "2026-10-02T12:00");
            servico.cancelar(cancelada.id(), "caio");
            reservar("aquario", "davi", 2, "2026-10-03T09:00", "2026-10-03T10:00");
            reservar("foco", "eva", 2, "2026-10-02T09:00", "2026-10-02T10:00");

            assertThat(servico.agenda("aquario", LocalDate.parse("2026-10-02"))).extracting(Reserva::id)
                    .containsExactly(manha.id(), tarde.id());
        }
    }

    @Nested
    @SpringBootTest(classes = {ReservasApplication.class, Http.RelogioFixo.class})
    @AutoConfigureMockMvc
    @DisplayName("o adaptador web (HTTP)")
    class Http {

        @TestConfiguration(proxyBeanMethods = false)
        static class RelogioFixo {
            @Bean
            @Primary
            Clock relogioDeTeste() {
                return RELOGIO;
            }
        }

        @Autowired
        MockMvcTester mvc;

        MvcTestResult reservar(String usuario, String sala, int pessoas, String inicio, String fim) {
            var requisicao = mvc.post().uri("/reservas").contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"sala": "%s", "participantes": %d, "inicio": "%s", "fim": "%s"}"""
                            .formatted(sala, pessoas, inicio, fim));
            if (usuario != null) {
                requisicao = requisicao.header("X-Usuario", usuario);
            }
            return requisicao.exchange();
        }

        String corpo(MvcTestResult r) {
            try {
                return r.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            } catch (java.io.UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Test
        @DisplayName("POST /reservas → 201, Location e o JSON da reserva")
        void cria() throws Exception {
            MvcTestResult r = reservar("ana", "auditorio", 30, "2026-10-05T14:00:00", "2026-10-05T16:00:00");

            assertThat(r).hasStatus(HttpStatus.CREATED);
            String id = JsonPath.read(corpo(r), "$.id");
            assertThat(r.getResponse().getHeader("Location")).endsWith("/reservas/" + id);
            assertThat(JsonPath.<String>read(corpo(r), "$.responsavel")).isEqualTo("ana");
            assertThat(JsonPath.<String>read(corpo(r), "$.status")).isEqualTo("ATIVA");
            assertThat(JsonPath.<String>read(corpo(r), "$.inicio")).startsWith("2026-10-05T14:00");
        }

        @Test
        @DisplayName("conflito → 409; regra violada → 422; sala inexistente → 404; sem X-Usuario → 400")
        void erros() {
            assertThat(reservar("ana", "auditorio", 10, "2026-10-06T10:00:00", "2026-10-06T11:00:00"))
                    .hasStatus(HttpStatus.CREATED);
            assertThat(reservar("bia", "auditorio", 10, "2026-10-06T10:30:00", "2026-10-06T11:30:00"))
                    .hasStatus(HttpStatus.CONFLICT);
            assertThat(reservar("bia", "auditorio", 10, "2026-10-06T21:00:00", "2026-10-06T22:00:00"))
                    .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
            assertThat(reservar("bia", "cozinha", 2, "2026-10-06T10:00:00", "2026-10-06T11:00:00"))
                    .hasStatus(HttpStatus.NOT_FOUND);
            assertThat(reservar(null, "foco", 1, "2026-10-06T10:00:00", "2026-10-06T11:00:00"))
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("DELETE /reservas/{id}: 403 para outra pessoa, 204 para o responsável, 404 se não existe")
        void cancela() throws Exception {
            MvcTestResult criada = reservar("ana", "foco", 2, "2026-10-07T10:00:00", "2026-10-07T11:00:00");
            String id = JsonPath.read(corpo(criada), "$.id");

            assertThat(mvc.delete().uri("/reservas/{id}", id).header("X-Usuario", "bia")).hasStatus(HttpStatus.FORBIDDEN);
            assertThat(mvc.delete().uri("/reservas/{id}", id).header("X-Usuario", "ana")).hasStatus(HttpStatus.NO_CONTENT);
            assertThat(mvc.delete().uri("/reservas/{id}", UUID.randomUUID()).header("X-Usuario", "ana"))
                    .hasStatus(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("GET /salas/{sala}/agenda?dia= → as reservas ativas do dia, em ordem")
        void agenda() {
            reservar("ana", "aquario", 3, "2026-10-08T15:00:00", "2026-10-08T16:00:00");
            reservar("bia", "aquario", 3, "2026-10-08T09:00:00", "2026-10-08T10:00:00");

            MvcTestResult r = mvc.get().uri("/salas/aquario/agenda?dia=2026-10-08").exchange();
            assertThat(r).hasStatusOk();
            assertThat(JsonPath.<List<String>>read(corpo(r), "$[*].responsavel")).containsExactly("bia", "ana");
        }
    }

    @Nested
    @DisplayName("as regras da arquitetura hexagonal")
    class Arquitetura {

        final JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.trilhajava.m13.reservas");

        @Test
        @DisplayName("camadas em cebola: domínio no centro, aplicação em volta, adaptadores por fora e isolados entre si")
        void cebola() {
            for (String adaptador : List.of("web", "persistencia", "notificacao")) {
                assertThat(classes.stream().filter(c -> c.getPackageName().endsWith(".adaptador." + adaptador)))
                        .as("classes no pacote adaptador.%s", adaptador).isNotEmpty();
            }
            onionArchitecture()
                    .domainModels("dev.trilhajava.m13.reservas.dominio..")
                    .applicationServices("dev.trilhajava.m13.reservas.aplicacao..")
                    .adapter("web", "dev.trilhajava.m13.reservas.adaptador.web..")
                    .adapter("persistencia", "dev.trilhajava.m13.reservas.adaptador.persistencia..")
                    .adapter("notificacao", "dev.trilhajava.m13.reservas.adaptador.notificacao..")
                    .withOptionalLayers(true)
                    // O pacote raiz (a aplicação Spring e a montagem do núcleo) fica fora das camadas: é quem liga tudo.
                    .ignoreDependency(JavaClass.Predicates.resideInAPackage("dev.trilhajava.m13.reservas"),
                            DescribedPredicate.alwaysTrue())
                    .check(classes);
        }

        @Test
        @DisplayName("o núcleo (domínio e aplicação) não depende do Spring")
        void nucleoSemSpring() {
            noClasses().that().resideInAnyPackage("..reservas.dominio..", "..reservas.aplicacao..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .check(classes);
        }

        @Test
        @DisplayName("os adaptadores falam com o núcleo pelas portas: ninguém fora da aplicação usa o ServicoDeReservas")
        void pelasPortas() {
            assertThat(classes.stream().filter(c -> c.getPackageName().contains(".adaptador.")).count())
                    .as("classes nos adaptadores").isGreaterThanOrEqualTo(3);
            noClasses().that().resideInAPackage("..reservas.adaptador..")
                    .should().dependOnClassesThat().haveSimpleName("ServicoDeReservas")
                    .check(classes);
            classes().that().resideInAnyPackage("..porta.entrada..", "..porta.saida..")
                    .and().areTopLevelClasses()
                    .should().beInterfaces()
                    .check(classes);
        }
    }
}
