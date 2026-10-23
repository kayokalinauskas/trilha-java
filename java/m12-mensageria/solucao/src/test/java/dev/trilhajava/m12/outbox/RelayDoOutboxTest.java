package dev.trilhajava.m12.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m12.outbox.EventoDeSaida.Status;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 12.03 — relay do transactional outbox")
class RelayDoOutboxTest {

    static final class RelogioDeTeste extends Clock {
        private Instant agora = Instant.parse("2026-09-29T12:00:00Z");

        void avancar(Duration quanto) {
            agora = agora.plus(quanto);
        }

        @Override public Instant instant() { return agora; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return this; }
    }

    /** Registra o que foi publicado; falhas programadas por agregado. */
    static final class BrokerDeTeste implements Publicador {
        final List<String> publicados = new ArrayList<>();
        final Map<String, Supplier<RuntimeException>> falhas = new HashMap<>();

        void falharPara(String agregado, Supplier<RuntimeException> erro) {
            falhas.put(agregado, erro);
        }

        void voltarAoNormal(String agregado) {
            falhas.remove(agregado);
        }

        @Override
        public void publicar(EventoDeSaida evento) {
            Supplier<RuntimeException> erro = falhas.get(evento.agregado());
            if (erro != null) {
                throw erro.get();
            }
            publicados.add(evento.payload());
        }
    }

    final RelogioDeTeste relogio = new RelogioDeTeste();
    final RepositorioDeSaida repositorio = new RepositorioDeSaidaEmMemoria();
    final BrokerDeTeste broker = new BrokerDeTeste();

    RelayDoOutbox relay(ConfiguracaoDoRelay config) {
        return new RelayDoOutbox(repositorio, broker, relogio, config);
    }

    RelayDoOutbox relay() {
        return relay(ConfiguracaoDoRelay.padrao());
    }

    EventoDeSaida gravar(String agregado, String payload) {
        EventoDeSaida e = EventoDeSaida.novo(agregado, "Teste", payload, relogio.instant());
        repositorio.salvar(e);
        relogio.avancar(Duration.ofMillis(1));
        return e;
    }

    EventoDeSaida atual(EventoDeSaida e) {
        return repositorio.buscar(e.id()).orElseThrow();
    }

    static Supplier<RuntimeException> foraDoAr() {
        return () -> new IllegalStateException("broker fora do ar");
    }

    @Test
    @DisplayName("publica os pendentes na ordem de criação e marca como PUBLICADO")
    void publica() {
        EventoDeSaida a1 = gravar("A", "a1");
        gravar("B", "b1");
        gravar("A", "a2");
        gravar("B", "b2");

        Resultado r = relay().executar();

        assertThat(r).isEqualTo(new Resultado(4, 0, 0));
        assertThat(broker.publicados).containsExactly("a1", "b1", "a2", "b2");
        assertThat(atual(a1).status()).isEqualTo(Status.PUBLICADO);
        assertThat(atual(a1).publicadoEm()).isEqualTo(relogio.instant());
    }

    @Test
    @DisplayName("não publica de novo o que já foi publicado")
    void naoRepublica() {
        gravar("A", "a1");
        relay().executar();

        assertThat(relay().executar()).isEqualTo(new Resultado(0, 0, 0));
        assertThat(broker.publicados).containsExactly("a1");
    }

    @Test
    @DisplayName("respeita o tamanho do lote")
    void lote() {
        for (int i = 1; i <= 5; i++) {
            gravar("A" + i, "e" + i);
        }
        RelayDoOutbox relay = relay(new ConfiguracaoDoRelay(2, 10, Duration.ofSeconds(1), Duration.ofMinutes(5)));

        assertThat(relay.executar().publicados()).isEqualTo(2);
        assertThat(broker.publicados).containsExactly("e1", "e2");
        relay.executar();
        relay.executar();
        assertThat(broker.publicados).containsExactly("e1", "e2", "e3", "e4", "e5");
    }

    @Test
    @DisplayName("falha: continua PENDENTE, conta a tentativa, guarda o erro e agenda a próxima para daqui a 1 s")
    void falha() {
        EventoDeSaida a1 = gravar("A", "a1");
        broker.falharPara("A", foraDoAr());

        Resultado r = relay().executar();

        assertThat(r).isEqualTo(new Resultado(0, 1, 0));
        EventoDeSaida depois = atual(a1);
        assertThat(depois.status()).isEqualTo(Status.PENDENTE);
        assertThat(depois.tentativas()).isEqualTo(1);
        assertThat(depois.proximaTentativa()).isEqualTo(relogio.instant().plusSeconds(1));
        assertThat(depois.ultimoErro()).contains("broker fora do ar");
    }

    @Test
    @DisplayName("antes da hora, não tenta de novo; na hora, tenta")
    void esperaAHora() {
        EventoDeSaida a1 = gravar("A", "a1");
        broker.falharPara("A", foraDoAr());
        relay().executar();
        broker.voltarAoNormal("A");

        relogio.avancar(Duration.ofMillis(999));
        assertThat(relay().executar()).isEqualTo(new Resultado(0, 0, 1));

        relogio.avancar(Duration.ofMillis(1));
        assertThat(relay().executar()).isEqualTo(new Resultado(1, 0, 0));
        assertThat(atual(a1).status()).isEqualTo(Status.PUBLICADO);
    }

    @Test
    @DisplayName("backoff exponencial: 1 s, 2 s, 4 s, 8 s... com teto de 5 min")
    void backoff() {
        EventoDeSaida a1 = gravar("A", "a1");
        broker.falharPara("A", foraDoAr());
        RelayDoOutbox relay = relay(new ConfiguracaoDoRelay(10, 100, Duration.ofSeconds(1), Duration.ofMinutes(5)));

        List<Long> esperas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            relay.executar();
            EventoDeSaida e = atual(a1);
            esperas.add(Duration.between(relogio.instant(), e.proximaTentativa()).toSeconds());
            relogio.avancar(Duration.between(relogio.instant(), e.proximaTentativa()));
        }
        assertThat(esperas).containsExactly(1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L, 256L, 300L, 300L, 300L);
    }

    @Test
    @DisplayName("ordem por agregado: se A1 falha, A2 espera; B segue normalmente")
    void ordemPorAgregado() {
        gravar("A", "a1");
        gravar("A", "a2");
        gravar("B", "b1");
        broker.falharPara("A", foraDoAr());

        Resultado r = relay().executar();

        assertThat(r).isEqualTo(new Resultado(1, 1, 1));
        assertThat(broker.publicados).containsExactly("b1");
    }

    @Test
    @DisplayName("enquanto A1 está esperando a próxima tentativa, A2 também espera")
    void a2EsperaA1() {
        gravar("A", "a1");
        broker.falharPara("A", foraDoAr());
        relay().executar();
        broker.voltarAoNormal("A");
        gravar("A", "a2");

        assertThat(relay().executar()).isEqualTo(new Resultado(0, 0, 2));
        assertThat(broker.publicados).isEmpty();

        relogio.avancar(Duration.ofSeconds(1));
        relay().executar();
        assertThat(broker.publicados).containsExactly("a1", "a2");
    }

    @Test
    @DisplayName("qualquer exceção do publicador conta como falha e o relay continua")
    void excecaoInesperada() {
        gravar("A", "a1");
        gravar("B", "b1");
        broker.falharPara("A", () -> new NullPointerException("bug no serializador"));

        assertThat(relay().executar()).isEqualTo(new Resultado(1, 1, 0));
        assertThat(broker.publicados).containsExactly("b1");
    }

    @Test
    @DisplayName("na última tentativa permitida, o evento vira MORTO e não é mais tentado")
    void morre() {
        EventoDeSaida a1 = gravar("A", "a1");
        broker.falharPara("A", foraDoAr());
        RelayDoOutbox relay = relay(new ConfiguracaoDoRelay(10, 3, Duration.ofSeconds(1), Duration.ofMinutes(5)));

        for (int i = 0; i < 3; i++) {
            assertThat(relay.executar().falhas()).isEqualTo(1);
            relogio.avancar(Duration.ofMinutes(10));
        }
        assertThat(atual(a1).status()).isEqualTo(Status.MORTO);
        assertThat(atual(a1).tentativas()).isEqualTo(3);
        assertThat(relay.executar()).isEqualTo(new Resultado(0, 0, 0));
    }

    @Test
    @DisplayName("um evento MORTO trava o agregado; reprocessar libera e a ordem se mantém")
    void mortoTrava() {
        EventoDeSaida a1 = gravar("A", "a1");
        broker.falharPara("A", foraDoAr());
        RelayDoOutbox relay = relay(new ConfiguracaoDoRelay(10, 1, Duration.ofSeconds(1), Duration.ofMinutes(5)));
        relay.executar();
        assertThat(atual(a1).status()).isEqualTo(Status.MORTO);

        broker.voltarAoNormal("A");
        gravar("A", "a2");
        gravar("B", "b1");
        assertThat(relay.executar()).isEqualTo(new Resultado(1, 0, 1));
        assertThat(broker.publicados).containsExactly("b1");

        relay.reprocessar(a1.id());
        assertThat(atual(a1).status()).isEqualTo(Status.PENDENTE);
        assertThat(atual(a1).tentativas()).isZero();
        relay.executar();
        assertThat(broker.publicados).containsExactly("b1", "a1", "a2");
    }

    @Test
    @DisplayName("reprocessar só vale para eventos MORTOS que existem")
    void reprocessarValida() {
        EventoDeSaida a1 = gravar("A", "a1");

        assertThatThrownBy(() -> relay().reprocessar(a1.id())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> relay().reprocessar(UUID.randomUUID())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("limpar apaga só os publicados mais antigos que a retenção")
    void limpar() {
        EventoDeSaida velho = gravar("A", "velho");
        relay().executar();
        relogio.avancar(Duration.ofDays(8));
        EventoDeSaida recente = gravar("B", "recente");
        relay().executar();
        EventoDeSaida pendente = gravar("C", "pendente");
        broker.falharPara("C", foraDoAr());
        relay().executar();

        int removidos = relay().limpar(Duration.ofDays(7));

        assertThat(removidos).isEqualTo(1);
        assertThat(repositorio.buscar(velho.id())).isEmpty();
        assertThat(repositorio.buscar(recente.id())).isPresent();
        assertThat(repositorio.buscar(pendente.id())).isPresent();
    }
}
