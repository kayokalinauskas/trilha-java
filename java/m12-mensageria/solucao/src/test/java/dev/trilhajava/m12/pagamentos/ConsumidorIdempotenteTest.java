package dev.trilhajava.m12.pagamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import dev.trilhajava.m12.ContainersDeMensageria;
import dev.trilhajava.m12.pagamentos.EventoDePagamento.Tipo;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@SpringBootTest(classes = PagamentosApplication.class)
@Import(ContainersDeMensageria.Postgres.class)
@EnabledIfDockerAvailable
@DisplayName("Exercício 12.04 — consumidor idempotente (PostgreSQL)")
class ConsumidorIdempotenteTest {

    static final BigDecimal VALOR = new BigDecimal("259.90");

    @Autowired
    ConsumidorDePagamentos consumidor;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    DataSource dataSource;

    @MockitoBean
    NotificadorDePagamento notificador;

    @MockitoSpyBean
    Contabilidade contabilidade;

    String pedido;

    @BeforeEach
    void preparar() {
        jdbc.sql("TRUNCATE mensagem_processada, lancamento_contabil, pagamento, pedido").update();
        pedido = UUID.randomUUID().toString();
        jdbc.sql("INSERT INTO pedido (id, valor) VALUES (?, ?)").params(pedido, VALOR).update();
    }

    EventoDePagamento aprovado(String mensagemId, long sequencia) {
        return new EventoDePagamento(mensagemId, pedido, Tipo.APROVADO, VALOR, "tx-" + mensagemId, sequencia);
    }

    EventoDePagamento estornado(String mensagemId, long sequencia) {
        return new EventoDePagamento(mensagemId, pedido, Tipo.ESTORNADO, VALOR, "tx-" + mensagemId, sequencia);
    }

    String status() {
        return jdbc.sql("SELECT status FROM pedido WHERE id = ?").param(pedido).query(String.class).single();
    }

    long contar(String tabela) {
        return jdbc.sql("SELECT count(*) FROM " + tabela).query(Long.class).single();
    }

    @Test
    @DisplayName("aprovado: pedido PAGO, um pagamento, um lançamento e o cliente avisado")
    void aprovado() {
        assertThat(consumidor.processar(aprovado("m-1", 1))).isEqualTo(ResultadoDoProcessamento.PROCESSADO);

        assertThat(status()).isEqualTo("PAGO");
        assertThat(contar("pagamento")).isEqualTo(1);
        assertThat(contar("lancamento_contabil")).isEqualTo(1);
        assertThat(contar("mensagem_processada")).isEqualTo(1);
        verify(notificador).pagamentoAprovado(pedido);
    }

    @Test
    @DisplayName("a mesma mensagem de novo: DUPLICADO, nada muda e ninguém é avisado duas vezes")
    void duplicada() {
        consumidor.processar(aprovado("m-1", 1));

        assertThat(consumidor.processar(aprovado("m-1", 1))).isEqualTo(ResultadoDoProcessamento.DUPLICADO);
        assertThat(consumidor.processar(aprovado("m-1", 1))).isEqualTo(ResultadoDoProcessamento.DUPLICADO);

        assertThat(contar("pagamento")).isEqualTo(1);
        assertThat(contar("lancamento_contabil")).isEqualTo(1);
        verify(notificador, times(1)).pagamentoAprovado(pedido);
    }

    @Test
    @DisplayName("8 cópias ao mesmo tempo: exatamente 1 PROCESSADO e 7 DUPLICADO")
    void concorrentes() throws Exception {
        int copias = 8;
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<ResultadoDoProcessamento>> resultados = new ArrayList<>();
        try (ExecutorService threads = Executors.newFixedThreadPool(copias)) {
            for (int i = 0; i < copias; i++) {
                Callable<ResultadoDoProcessamento> tarefa = () -> {
                    largada.await();
                    return consumidor.processar(aprovado("m-concorrente", 1));
                };
                resultados.add(threads.submit(tarefa));
            }
            largada.countDown();
            List<ResultadoDoProcessamento> obtidos = new ArrayList<>();
            for (Future<ResultadoDoProcessamento> f : resultados) {
                obtidos.add(f.get());
            }
            assertThat(obtidos).filteredOn(r -> r == ResultadoDoProcessamento.PROCESSADO).hasSize(1);
            assertThat(obtidos).filteredOn(r -> r == ResultadoDoProcessamento.DUPLICADO).hasSize(copias - 1);
        }
        assertThat(contar("pagamento")).isEqualTo(1);
        assertThat(contar("lancamento_contabil")).isEqualTo(1);
        verify(notificador, times(1)).pagamentoAprovado(pedido);
    }

    @Test
    @DisplayName("fora de ordem: o estorno (seq 2) chega antes da aprovação (seq 1); a aprovação é OBSOLETA")
    void foraDeOrdem() {
        assertThat(consumidor.processar(estornado("m-2", 2))).isEqualTo(ResultadoDoProcessamento.PROCESSADO);
        assertThat(consumidor.processar(aprovado("m-1", 1))).isEqualTo(ResultadoDoProcessamento.OBSOLETO);

        assertThat(status()).isEqualTo("ESTORNADO");
        assertThat(contar("pagamento")).isEqualTo(1);
        verify(notificador, never()).pagamentoAprovado(anyString());
    }

    @Test
    @DisplayName("a mensagem OBSOLETA fica registrada: se chegar de novo, é DUPLICADA")
    void obsoletaRegistrada() {
        consumidor.processar(estornado("m-2", 2));
        consumidor.processar(aprovado("m-1", 1));

        assertThat(consumidor.processar(aprovado("m-1", 1))).isEqualTo(ResultadoDoProcessamento.DUPLICADO);
        assertThat(contar("mensagem_processada")).isEqualTo(2);
    }

    @Test
    @DisplayName("em ordem: aprovado (1) e depois estornado (2) aplicam os dois e lançam +valor e -valor")
    void emOrdem() {
        consumidor.processar(aprovado("m-1", 1));
        consumidor.processar(estornado("m-2", 2));

        assertThat(status()).isEqualTo("ESTORNADO");
        BigDecimal saldo = jdbc.sql("SELECT sum(valor) FROM lancamento_contabil WHERE pedido_id = ?")
                .param(pedido).query(BigDecimal.class).single();
        assertThat(saldo).isEqualByComparingTo("0");
        verify(notificador).pagamentoAprovado(pedido);
        verify(notificador).pagamentoEstornado(pedido);
    }

    @Test
    @DisplayName("falha no meio: tudo é desfeito (inclusive a marca de processada) e a reentrega funciona")
    void falhaNoMeio() {
        doThrow(new IllegalStateException("conexão caiu"))
                .doCallRealMethod()
                .when(contabilidade).lancar(anyString(), any(), anyString());

        assertThatThrownBy(() -> consumidor.processar(aprovado("m-1", 1))).isInstanceOf(IllegalStateException.class);
        assertThat(status()).isEqualTo("AGUARDANDO");
        assertThat(contar("pagamento")).isZero();
        assertThat(contar("mensagem_processada")).isZero();
        verifyNoInteractions(notificador);

        assertThat(consumidor.processar(aprovado("m-1", 1))).isEqualTo(ResultadoDoProcessamento.PROCESSADO);
        assertThat(contar("pagamento")).isEqualTo(1);
        verify(notificador, times(1)).pagamentoAprovado(pedido);
    }

    @Test
    @DisplayName("pedido desconhecido: exceção, nada gravado")
    void desconhecido() {
        var evento = new EventoDePagamento("m-x", "nao-existe", Tipo.APROVADO, VALOR, "tx", 1);

        assertThatThrownBy(() -> consumidor.processar(evento)).isInstanceOf(PedidoDesconhecidoException.class);
        assertThat(contar("mensagem_processada")).isZero();
    }

    @Test
    @DisplayName("valor divergente: exceção, nada gravado, ninguém avisado")
    void valorDivergente() {
        var evento = new EventoDePagamento("m-1", pedido, Tipo.APROVADO, new BigDecimal("1.00"), "tx", 1);

        assertThatThrownBy(() -> consumidor.processar(evento)).isInstanceOf(ValorDivergenteException.class);
        assertThat(status()).isEqualTo("AGUARDANDO");
        assertThat(contar("mensagem_processada")).isZero();
        assertThat(contar("lancamento_contabil")).isZero();
        verifyNoInteractions(notificador);
    }

    @Test
    @DisplayName("o cliente só é avisado depois do commit: nesse momento, outra conexão já vê o pagamento")
    void avisoDepoisDoCommit() {
        List<Long> pagamentosVisiveis = new ArrayList<>();
        doAnswer(invocacao -> {
            // Uma conexão NOVA do pool só enxerga o que já foi confirmado (commit).
            try (Connection outra = dataSource.getConnection();
                    var consulta = outra.prepareStatement("SELECT count(*) FROM pagamento WHERE pedido_id = ?")) {
                consulta.setString(1, pedido);
                try (ResultSet rs = consulta.executeQuery()) {
                    rs.next();
                    pagamentosVisiveis.add(rs.getLong(1));
                }
            }
            return null;
        }).when(notificador).pagamentoAprovado(anyString());

        consumidor.processar(aprovado("m-1", 1));

        assertThat(pagamentosVisiveis).as("pagamentos já confirmados quando o aviso saiu").containsExactly(1L);
    }
}
