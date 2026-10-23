package dev.trilhajava.m11.limite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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

/**
 * Confere a solução de referência do exercício 11.04 contra os exemplos do enunciado (garante que o que o site
 * mostra funciona). Não faz parte do starter.
 */
@DisplayName("Exercício 11.04 — limite de requisições (token bucket)")
class LimiteDeRequisicoesTest {

    /** Um relógio que só anda quando o teste manda. */
    static final class RelogioDeTeste extends Clock {
        private volatile Instant agora = Instant.parse("2026-09-29T12:00:00Z");

        void avancar(Duration quanto) {
            agora = agora.plus(quanto);
        }

        @Override public Instant instant() { return agora; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return this; }
    }

    @Nested
    @DisplayName("BaldeDeFichas")
    class Balde {

        final RelogioDeTeste relogio = new RelogioDeTeste();
        final BaldeDeFichas balde = new BaldeDeFichas(5, Duration.ofMinutes(1), relogio);

        void esvaziar() {
            for (int i = 0; i < 5; i++) {
                assertThat(balde.tentarConsumir()).as("ficha %d", i + 1).isTrue();
            }
        }

        @Test
        @DisplayName("começa cheio: 5 consumos passam, o 6º não")
        void comecaCheio() {
            assertThat(balde.fichasDisponiveis()).isEqualTo(5);
            esvaziar();
            assertThat(balde.tentarConsumir()).isFalse();
            assertThat(balde.fichasDisponiveis()).isZero();
        }

        @Test
        @DisplayName("repõe uma ficha a cada 12 s (5 por minuto)")
        void repoe() {
            esvaziar();
            relogio.avancar(Duration.ofSeconds(11));
            assertThat(balde.tentarConsumir()).isFalse();
            relogio.avancar(Duration.ofSeconds(1));
            assertThat(balde.tentarConsumir()).isTrue();
            assertThat(balde.tentarConsumir()).isFalse();
        }

        @Test
        @DisplayName("frações de tempo se acumulam (6 s + 6 s = 1 ficha)")
        void fracoes() {
            esvaziar();
            relogio.avancar(Duration.ofSeconds(6));
            assertThat(balde.tentarConsumir()).isFalse();
            relogio.avancar(Duration.ofSeconds(6));
            assertThat(balde.tentarConsumir()).isTrue();
        }

        @Test
        @DisplayName("tentativas recusadas não gastam o tempo acumulado")
        void recusaNaoGasta() {
            esvaziar();
            for (int i = 0; i < 11; i++) {
                relogio.avancar(Duration.ofSeconds(1));
                assertThat(balde.tentarConsumir()).isFalse();
            }
            relogio.avancar(Duration.ofSeconds(1));
            assertThat(balde.tentarConsumir()).isTrue();
        }

        @Test
        @DisplayName("nunca passa da capacidade, mesmo depois de horas parado")
        void capacidade() {
            balde.tentarConsumir();
            relogio.avancar(Duration.ofHours(3));
            assertThat(balde.fichasDisponiveis()).isEqualTo(5);
        }

        @Test
        @DisplayName("informa quanto falta para a próxima ficha")
        void tempoAteProxima() {
            assertThat(balde.tempoAteProximaFicha()).isZero();
            esvaziar();
            assertThat(balde.tempoAteProximaFicha()).isEqualTo(Duration.ofSeconds(12));
            relogio.avancar(Duration.ofSeconds(5));
            assertThat(balde.tempoAteProximaFicha()).isEqualTo(Duration.ofSeconds(7));
        }

        @Test
        @DisplayName("relógio voltando no tempo não cria nem remove fichas")
        void relogioVoltando() {
            esvaziar();
            relogio.avancar(Duration.ofSeconds(-30));
            assertThat(balde.tentarConsumir()).isFalse();
            relogio.avancar(Duration.ofSeconds(30));
            relogio.avancar(Duration.ofSeconds(12));
            assertThat(balde.tentarConsumir()).isTrue();
        }

        @Test
        @DisplayName("100 threads disputando 50 fichas: exatamente 50 conseguem")
        void concorrencia() throws Exception {
            var grande = new BaldeDeFichas(50, Duration.ofMinutes(1), relogio);
            var largada = new CountDownLatch(1);
            var sucessos = new AtomicInteger();
            try (var executor = Executors.newFixedThreadPool(16)) {
                for (int i = 0; i < 100; i++) {
                    executor.submit(() -> {
                        largada.await();
                        if (grande.tentarConsumir()) {
                            sucessos.incrementAndGet();
                        }
                        return null;
                    });
                }
                largada.countDown();
            }
            assertThat(sucessos).hasValue(50);
        }

        @Test
        @DisplayName("capacidade ou período inválidos → IllegalArgumentException")
        void invalidos() {
            assertThatThrownBy(() -> new BaldeDeFichas(0, Duration.ofMinutes(1), relogio))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new BaldeDeFichas(5, Duration.ZERO, relogio))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("LimitadorPorChave")
    class Limitador {

        final RelogioDeTeste relogio = new RelogioDeTeste();
        final LimitadorPorChave limitador = new LimitadorPorChave(2, Duration.ofSeconds(10), relogio);

        @Test
        @DisplayName("cada chave tem o seu balde")
        void chavesIndependentes() {
            assertThat(limitador.tentar("a").permitida()).isTrue();
            assertThat(limitador.tentar("a").permitida()).isTrue();
            assertThat(limitador.tentar("a").permitida()).isFalse();
            assertThat(limitador.tentar("b").permitida()).isTrue();
        }

        @Test
        @DisplayName("a decisão informa as fichas restantes e quanto esperar")
        void decisao() {
            assertThat(limitador.tentar("x")).isEqualTo(new Decisao(true, 1, Duration.ZERO));
            assertThat(limitador.tentar("x")).isEqualTo(new Decisao(true, 0, Duration.ofSeconds(5)));
            assertThat(limitador.tentar("x")).isEqualTo(new Decisao(false, 0, Duration.ofSeconds(5)));
            relogio.avancar(Duration.ofSeconds(2));
            assertThat(limitador.tentar("x")).isEqualTo(new Decisao(false, 0, Duration.ofSeconds(3)));
        }
    }

    @Nested
    @SpringBootTest(classes = {LimiteApplication.class, LimiteDeRequisicoesTest.RelogioDaApi.class})
    @AutoConfigureMockMvc
    @DisplayName("FiltroDeLimite no POST /login")
    class Filtro {

        @Autowired
        MockMvcTester mvc;

        @Autowired
        RelogioDeTeste relogio;

        MvcTestResult login(String ip) {
            return mvc.post().uri("/login").contentType(MediaType.APPLICATION_JSON).content("{}")
                    .with(requisicao -> {
                        requisicao.setRemoteAddr(ip);
                        return requisicao;
                    })
                    .exchange();
        }

        @Test
        @DisplayName("5 tentativas passam; a 6ª recebe 429 com Retry-After e problem+json")
        void limite() {
            for (int i = 0; i < 5; i++) {
                assertThat(login("10.0.0.1")).as("tentativa %d", i + 1).hasStatusOk();
            }
            MvcTestResult barrada = login("10.0.0.1");
            assertThat(barrada).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(barrada).hasHeader("Retry-After", "12");
            assertThat(barrada).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
            String corpo = new String(barrada.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
            assertThat(JsonPath.<String>read(corpo, "$.type"))
                    .isEqualTo("https://api.exemplo.dev/problemas/muitas-requisicoes");
            assertThat(JsonPath.<Integer>read(corpo, "$.status")).isEqualTo(429);
        }

        @Test
        @DisplayName("o limite é por IP")
        void porIp() {
            for (int i = 0; i < 6; i++) {
                login("10.0.0.2");
            }
            assertThat(login("10.0.0.2")).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(login("10.0.0.3")).hasStatusOk();
        }

        @Test
        @DisplayName("depois de esperar o Retry-After, uma nova tentativa passa")
        void esperar() {
            for (int i = 0; i < 5; i++) {
                login("10.0.0.4");
            }
            assertThat(login("10.0.0.4")).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
            relogio.avancar(Duration.ofSeconds(12));
            assertThat(login("10.0.0.4")).hasStatusOk();
            assertThat(login("10.0.0.4")).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
        }

        @Test
        @DisplayName("outro método no /login não gasta ficha")
        void outroMetodo() {
            for (int i = 0; i < 10; i++) {
                mvc.get().uri("/login").with(requisicao -> {
                    requisicao.setRemoteAddr("10.0.0.6");
                    return requisicao;
                }).exchange();
            }
            assertThat(login("10.0.0.6")).hasStatusOk();
        }

        @Test
        @DisplayName("outras rotas não são limitadas")
        void outrasRotas() {
            for (int i = 0; i < 20; i++) {
                assertThat(mvc.get().uri("/status").with(requisicao -> {
                    requisicao.setRemoteAddr("10.0.0.5");
                    return requisicao;
                }).exchange()).hasStatusOk();
            }
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RelogioDaApi {

        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }
    }
}
