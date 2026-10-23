package dev.trilhajava.m13.cambio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 13.04 — cliente de câmbio resiliente (timeout, retry, circuit breaker e fallback)")
class ClienteDeCambioTest {

    static final class RelogioDeTeste extends Clock {
        private volatile Instant agora = Instant.parse("2026-09-29T12:00:00Z");

        void avancar(Duration quanto) {
            agora = agora.plus(quanto);
        }

        @Override public Instant instant() { return agora; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return this; }
    }

    /** Uma resposta programada do provedor falso. */
    record Resposta(int status, String corpo, long atrasoMs, long atrasoNoCorpoMs) {
        static Resposta ok(String valor) {
            return new Resposta(200, "{\"moeda\": \"USD\", \"valor\": \"" + valor + "\"}", 0, 0);
        }

        static Resposta erro(int status) {
            return new Resposta(status, "{\"erro\": \"" + status + "\"}", 0, 0);
        }

        /** Demora para mandar os cabeçalhos. */
        static Resposta lenta(long ms) {
            return new Resposta(200, "{\"moeda\": \"USD\", \"valor\": \"9.99\"}", ms, 0);
        }

        /** Manda os cabeçalhos e o começo do corpo na hora, e trava no meio do corpo. */
        static Resposta travaNoCorpo(long ms) {
            return new Resposta(200, "{\"moeda\": \"USD\", \"valor\": \"9.99\"}", 0, ms);
        }
    }

    /** Timeouts curtos, 2 repetições, circuito com janela de 4 chamadas e 50% de falhas. */
    static final ConfiguracaoDoCliente CONFIG = new ConfiguracaoDoCliente(Duration.ofMillis(500),
            Duration.ofMillis(300), 2, Duration.ofMillis(20), 4, 50f, Duration.ofSeconds(30), Duration.ofMinutes(10));

    HttpServer servidor;
    final AtomicInteger chamadas = new AtomicInteger();
    final ConcurrentLinkedDeque<Resposta> roteiro = new ConcurrentLinkedDeque<>();
    volatile Resposta padrao = Resposta.ok("5.43");
    final RelogioDeTeste relogio = new RelogioDeTeste();
    ClienteDeCambio cliente;

    @BeforeEach
    void subirProvedorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.setExecutor(Executors.newCachedThreadPool());
        servidor.createContext("/cotacoes/", troca -> {
            chamadas.incrementAndGet();
            Resposta r = roteiro.isEmpty() ? padrao : roteiro.poll();
            try {
                if (r.atrasoMs() > 0) {
                    Thread.sleep(r.atrasoMs());
                }
                byte[] corpo = r.corpo().getBytes(StandardCharsets.UTF_8);
                troca.getResponseHeaders().add("Content-Type", "application/json");
                troca.sendResponseHeaders(r.status(), corpo.length);
                try (OutputStream saida = troca.getResponseBody()) {
                    if (r.atrasoNoCorpoMs() > 0) {
                        saida.write(corpo, 0, 5);
                        saida.flush();
                        Thread.sleep(r.atrasoNoCorpoMs());
                        saida.write(corpo, 5, corpo.length - 5);
                    } else {
                        saida.write(corpo);
                    }
                }
            } catch (InterruptedException | IOException e) {
                troca.close();
            }
        });
        servidor.start();
        cliente = new ClienteDeCambio("http://localhost:" + servidor.getAddress().getPort(), CONFIG, relogio);
    }

    @AfterEach
    void derrubar() {
        servidor.stop(0);
    }

    void programar(Resposta... respostas) {
        roteiro.addAll(java.util.List.of(respostas));
    }

    @Test
    @DisplayName("200: devolve a cotação atual, não desatualizada")
    void sucesso() {
        Cotacao c = cliente.cotacao("USD");

        assertThat(c.valor()).isEqualByComparingTo("5.43");
        assertThat(c.desatualizada()).isFalse();
        assertThat(c.obtidaEm()).isEqualTo(relogio.instant());
        assertThat(chamadas).hasValue(1);
    }

    @Test
    @DisplayName("503, 503, 200: repete e consegue (3 chamadas)")
    void repeteFalhaTransitoria() {
        programar(Resposta.erro(503), Resposta.erro(503), Resposta.ok("5.50"));

        assertThat(cliente.cotacao("USD").valor()).isEqualByComparingTo("5.50");
        assertThat(chamadas).hasValue(3);
    }

    @Test
    @DisplayName("503 sempre, sem valor anterior: 1 + 2 repetições e CambioIndisponivel")
    void esgota() {
        padrao = Resposta.erro(503);

        assertThatThrownBy(() -> cliente.cotacao("USD")).isInstanceOf(CambioIndisponivelException.class);
        assertThat(chamadas).hasValue(3);
    }

    @Test
    @DisplayName("404: erro de quem pediu — MoedaDesconhecida, sem repetir")
    void naoRepete404() {
        padrao = Resposta.erro(404);

        assertThatThrownBy(() -> cliente.cotacao("XYZ")).isInstanceOf(MoedaDesconhecidaException.class);
        assertThat(chamadas).hasValue(1);
    }

    @Test
    @DisplayName("resposta mais lenta que o timeout de leitura conta como falha transitória, e é repetida")
    void timeout() {
        programar(Resposta.lenta(1_500), Resposta.ok("5.44"));
        long inicio = System.nanoTime();

        assertThat(cliente.cotacao("USD").valor()).isEqualByComparingTo("5.44");

        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(ms).as("não esperou a resposta lenta inteira (%d ms)", ms).isLessThan(1_200);
        assertThat(chamadas).hasValue(2);
    }

    @Test
    @DisplayName("cabeçalhos na hora e corpo travado: o timeout de leitura também corta, e a chamada é repetida")
    void timeoutNoCorpo() {
        programar(Resposta.travaNoCorpo(1_500), Resposta.ok("5.45"));
        long inicio = System.nanoTime();

        assertThat(cliente.cotacao("USD").valor()).isEqualByComparingTo("5.45");

        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(ms).as("não esperou o corpo travado inteiro (%d ms)", ms).isLessThan(1_200);
        assertThat(chamadas).hasValue(2);
    }

    @Test
    @DisplayName("fallback: com o provedor fora, devolve o último valor conhecido, marcado como desatualizado")
    void fallback() {
        Cotacao antes = cliente.cotacao("USD");
        relogio.avancar(Duration.ofMinutes(5));
        padrao = Resposta.erro(503);

        Cotacao depois = cliente.cotacao("USD");

        assertThat(depois.valor()).isEqualByComparingTo("5.43");
        assertThat(depois.desatualizada()).isTrue();
        assertThat(depois.obtidaEm()).isEqualTo(antes.obtidaEm());
    }

    @Test
    @DisplayName("o último valor só serve enquanto for recente (validade de 10 min)")
    void validade() {
        cliente.cotacao("USD");
        relogio.avancar(Duration.ofMinutes(11));
        padrao = Resposta.erro(503);

        assertThatThrownBy(() -> cliente.cotacao("USD")).isInstanceOf(CambioIndisponivelException.class);
    }

    @Test
    @DisplayName("circuito: depois de 4 chamadas com falha, abre e para de chamar o provedor")
    void abreOCircuito() {
        padrao = Resposta.erro(503);
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> cliente.cotacao("USD")).isInstanceOf(CambioIndisponivelException.class);
        }
        assertThat(cliente.circuitoAberto()).isTrue();
        int antes = chamadas.get();

        assertThatThrownBy(() -> cliente.cotacao("USD")).isInstanceOf(CambioIndisponivelException.class);
        assertThat(chamadas).as("com o circuito aberto, o provedor não é chamado").hasValue(antes);
    }

    @Test
    @DisplayName("circuito aberto ainda devolve o último valor recente")
    void abertoComFallback() {
        cliente.cotacao("USD");
        padrao = Resposta.erro(503);
        for (int i = 0; i < 4; i++) {
            cliente.cotacao("USD");
        }
        assertThat(cliente.circuitoAberto()).isTrue();

        assertThat(cliente.cotacao("USD").desatualizada()).isTrue();
    }

    @Test
    @DisplayName("depois do tempo aberto, uma chamada de teste passa; se der certo, o circuito fecha")
    void meioAberto() {
        padrao = Resposta.erro(503);
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> cliente.cotacao("USD")).isInstanceOf(CambioIndisponivelException.class);
        }
        padrao = Resposta.ok("5.60");
        relogio.avancar(Duration.ofSeconds(31));

        assertThat(cliente.cotacao("USD").valor()).isEqualByComparingTo("5.60");
        assertThat(cliente.circuitoAberto()).isFalse();
    }

    @Test
    @DisplayName("404 não é falha do provedor: não abre o circuito")
    void quatrocentosNaoAbrem() {
        padrao = Resposta.erro(404);
        for (int i = 0; i < 6; i++) {
            assertThatThrownBy(() -> cliente.cotacao("XYZ")).isInstanceOf(MoedaDesconhecidaException.class);
        }
        assertThat(cliente.circuitoAberto()).isFalse();
    }
}
