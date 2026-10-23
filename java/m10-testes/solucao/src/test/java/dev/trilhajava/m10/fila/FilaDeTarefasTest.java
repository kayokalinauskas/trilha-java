package dev.trilhajava.m10.fila;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * A suíte que a pessoa escreve no exercício 10.03 (solução de referência): prova, contra um PostgreSQL 18 de
 * verdade, cada regra do enunciado. Sem Docker, os testes são pulados.
 */
@SpringBootTest
@Import(ContainersDeTeste.class)
@EnabledIfDockerAvailable // sem Docker, os testes são PULADOS (e não "passam")
@DisplayName("Exercício 10.03 — Fila de tarefas no PostgreSQL com Testcontainers")
class FilaDeTarefasTest {

    @Autowired
    FilaDeTarefas fila;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    DataSource dataSource;

    @Autowired
    TransactionTemplate transacao;

    /** O container é compartilhado entre os testes: cada um começa com a tabela vazia. */
    @BeforeEach
    void limpar() {
        jdbc.sql("DELETE FROM tarefa").update();
    }

    @Test
    @DisplayName("regra 7: os testes rodam num PostgreSQL 18 de verdade (não H2)")
    void postgres18() throws Exception {
        try (Connection conexao = dataSource.getConnection()) {
            assertThat(conexao.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            assertThat(conexao.getMetaData().getDatabaseMajorVersion()).isEqualTo(18);
        }
    }

    @Test
    @DisplayName("regra 1: enfileirar cria PENDENTE, com 0 tentativas, e o JSONB normaliza o payload")
    void enfileirar() {
        long id = fila.enfileirar("email", "{\"pedido\":1,   \"cliente\":\"ana\"}");

        var linha = jdbc.sql("SELECT status, tentativas, payload::text AS payload FROM tarefa WHERE id = :id")
                .param("id", id).query().singleRow();
        assertThat(linha).containsEntry("status", "PENDENTE").containsEntry("tentativas", 0)
                .containsEntry("payload", "{\"pedido\": 1, \"cliente\": \"ana\"}");
        assertThat(fila.enfileirar("email", "{}")).isGreaterThan(id);
    }

    @Test
    @DisplayName("regra 1: JSON inválido é recusado pelo banco")
    void jsonInvalido() {
        assertThatThrownBy(() -> fila.enfileirar("email", "{pedido: 1}")).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(fila.contagemPorStatus()).containsEntry(StatusTarefa.PENDENTE, 0L);
    }

    @Test
    @DisplayName("regra 2: reserva na ordem de chegada (FIFO); fila vazia → Optional vazio")
    void fifo() {
        long primeira = fila.enfileirar("email", "{\"pedido\": 1}");
        long segunda = fila.enfileirar("email", "{\"pedido\": 2}");

        Tarefa t = fila.reservarProxima("trabalhador-a").orElseThrow();
        assertThat(t).isEqualTo(new Tarefa(primeira, "email", "{\"pedido\": 1}", StatusTarefa.EM_PROCESSAMENTO, 1,
                "trabalhador-a"));

        assertThat(fila.reservarProxima("trabalhador-b")).map(Tarefa::id).hasValue(segunda);
        assertThat(fila.reservarProxima("trabalhador-c")).isEmpty();
    }

    @Test
    @DisplayName("regra 3: uma tarefa travada por uma transação aberta é pulada, sem esperar")
    void pulaATravadaSemEsperar() {
        long primeira = fila.enfileirar("email", "{\"pedido\": 1}");
        long segunda = fila.enfileirar("email", "{\"pedido\": 2}");

        try (var outroTrabalhador = Executors.newSingleThreadExecutor()) {
            transacao.executeWithoutResult(status -> {
                // Dentro da transação, a reserva da primeira fica travada até o commit.
                assertThat(fila.reservarProxima("lento")).map(Tarefa::id).hasValue(primeira);

                Future<Optional<Tarefa>> rapido = outroTrabalhador.submit(() -> fila.reservarProxima("rapido"));
                assertThat(rapido).succeedsWithin(Duration.ofSeconds(2))
                        .satisfies(t -> assertThat(t).map(Tarefa::id).hasValue(segunda));
            });
        }
    }

    @Test
    @DisplayName("regra 4: concluir tira a tarefa da fila; tarefa que não está em processamento → IllegalStateException")
    void concluir() {
        long id = fila.enfileirar("relatorio", "{}");
        assertThatThrownBy(() -> fila.concluir(id)).isInstanceOf(IllegalStateException.class); // ainda PENDENTE

        fila.reservarProxima("t1");
        fila.concluir(id);

        assertThat(fila.contagemPorStatus()).containsEntry(StatusTarefa.CONCLUIDA, 1L)
                .containsEntry(StatusTarefa.PENDENTE, 0L);
        assertThat(fila.reservarProxima("t1")).isEmpty();
        assertThatThrownBy(() -> fila.concluir(id)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> fila.falhar(id, "x")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> fila.concluir(999_999)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("regra 5: falhar devolve a tarefa à fila, na mesma posição, e guarda o erro")
    void falharDevolveAFila() {
        long primeira = fila.enfileirar("cobranca", "{\"valor\": 10}");
        long segunda = fila.enfileirar("cobranca", "{\"valor\": 20}");

        fila.reservarProxima("t1");
        fila.falhar(primeira, "gateway fora do ar");

        var linha = jdbc.sql("SELECT status, reservada_por, ultimo_erro FROM tarefa WHERE id = :id")
                .param("id", primeira).query().singleRow();
        assertThat(linha).containsEntry("status", "PENDENTE").containsEntry("reservada_por", null)
                .containsEntry("ultimo_erro", "gateway fora do ar");

        Tarefa denovo = fila.reservarProxima("t2").orElseThrow();
        assertThat(denovo.id()).isEqualTo(primeira);
        assertThat(denovo.tentativas()).isEqualTo(2);
        assertThat(denovo.reservadaPor()).isEqualTo("t2");
        assertThat(fila.reservarProxima("t3")).map(Tarefa::id).hasValue(segunda);
    }

    @Test
    @DisplayName("regra 5: na 3ª falha, FALHOU — e a tarefa não é mais reservada")
    void retentativas() {
        long id = fila.enfileirar("cobranca", "{\"valor\": 10}");
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            Tarefa t = fila.reservarProxima("t1").orElseThrow();
            assertThat(t.tentativas()).isEqualTo(tentativa);
            fila.falhar(id, "gateway fora do ar (" + tentativa + ")");
        }

        assertThat(fila.reservarProxima("t1")).isEmpty();
        assertThat(fila.contagemPorStatus()).containsEntry(StatusTarefa.FALHOU, 1L);
        assertThat(jdbc.sql("SELECT ultimo_erro FROM tarefa WHERE id = :id").param("id", id).query(String.class)
                .single()).isEqualTo("gateway fora do ar (3)");
        assertThatThrownBy(() -> fila.falhar(id, "de novo")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("regra 6: a contagem traz os quatro status, mesmo com zero")
    void contagem() {
        assertThat(fila.contagemPorStatus()).containsExactlyInAnyOrderEntriesOf(Map.of(
                StatusTarefa.PENDENTE, 0L, StatusTarefa.EM_PROCESSAMENTO, 0L,
                StatusTarefa.CONCLUIDA, 0L, StatusTarefa.FALHOU, 0L));

        fila.enfileirar("a", "{}");
        fila.enfileirar("b", "{}");
        fila.reservarProxima("t1");

        assertThat(fila.contagemPorStatus()).containsEntry(StatusTarefa.PENDENTE, 1L)
                .containsEntry(StatusTarefa.EM_PROCESSAMENTO, 1L).containsEntry(StatusTarefa.CONCLUIDA, 0L);
    }

    @Test
    @DisplayName("regra 3: 8 trabalhadores concorrentes processam 200 tarefas — cada uma exatamente uma vez")
    void concorrencia() throws Exception {
        for (int i = 0; i < 200; i++) {
            fila.enfileirar("processar", "{\"n\": " + i + "}");
        }
        Map<Long, AtomicInteger> vezesProcessada = new ConcurrentHashMap<>();
        var largada = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Future<?>> trabalhadores = new ArrayList<>();
            for (int w = 0; w < 8; w++) {
                String nome = "trabalhador-" + w;
                trabalhadores.add(executor.submit(() -> {
                    largada.await(); // todos começam juntos, para maximizar a disputa
                    for (var t = fila.reservarProxima(nome); t.isPresent(); t = fila.reservarProxima(nome)) {
                        vezesProcessada.computeIfAbsent(t.get().id(), id -> new AtomicInteger()).incrementAndGet();
                        fila.concluir(t.get().id());
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (Future<?> trabalhador : trabalhadores) {
                trabalhador.get();
            }
        }

        assertThat(vezesProcessada).hasSize(200);
        assertThat(vezesProcessada.values()).allSatisfy(vezes -> assertThat(vezes).hasValue(1));
        assertThat(fila.contagemPorStatus()).containsEntry(StatusTarefa.CONCLUIDA, 200L);
    }
}
