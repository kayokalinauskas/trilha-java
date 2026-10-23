package dev.trilhajava.m14.metricas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.trilhajava.m14.metricas.Pagamento.MetodoDePagamento;
import dev.trilhajava.m14.metricas.ServicoDePagamentos.Resultado;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest(classes = MetricasApplication.class)
@AutoConfigureMockMvc
@AutoConfigureMetrics
@DisplayName("Exercício 14.02 — métricas de negócio com Micrometer")
class MetricasTest {

    @Autowired
    ServicoDePagamentos servico;

    @Autowired
    MeterRegistry registro;

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    Autorizador autorizador;

    /** O contexto é compartilhado entre os testes: compare deltas, não valores absolutos. */
    double processados(String metodo, String resultado) {
        Counter c = registro.find("pagamentos.processados").tag("metodo", metodo).tag("resultado", resultado).counter();
        return c == null ? 0 : c.count();
    }

    long autorizacoes(String metodo, String resultado) {
        Timer t = registro.find("pagamentos.autorizacao").tag("metodo", metodo).tag("resultado", resultado).timer();
        return t == null ? 0 : t.count();
    }

    @Test
    @DisplayName("aprovado: conta pagamentos.processados com metodo=pix e resultado=aprovado")
    void contaAprovado() {
        when(autorizador.autorizar(any())).thenReturn(true);
        double antes = processados("pix", "aprovado");

        assertThat(servico.processar(Pagamento.de("cli-1", MetodoDePagamento.PIX, "150.00"))).isEqualTo(Resultado.APROVADO);

        assertThat(processados("pix", "aprovado")).isEqualTo(antes + 1);
    }

    @Test
    @DisplayName("recusado: conta com resultado=recusado, separado dos aprovados")
    void contaRecusado() {
        when(autorizador.autorizar(any())).thenReturn(false);
        double aprovadosAntes = processados("cartao", "aprovado");
        double recusadosAntes = processados("cartao", "recusado");

        assertThat(servico.processar(Pagamento.de("cli-2", MetodoDePagamento.CARTAO, "99.00"))).isEqualTo(Resultado.RECUSADO);

        assertThat(processados("cartao", "recusado")).isEqualTo(recusadosAntes + 1);
        assertThat(processados("cartao", "aprovado")).isEqualTo(aprovadosAntes);
    }

    @Test
    @DisplayName("erro no provedor: a exceção sobe, mas o pagamento é contado com resultado=erro")
    void contaErro() {
        when(autorizador.autorizar(any())).thenThrow(new IllegalStateException("provedor fora do ar"));
        double antes = processados("boleto", "erro");

        assertThatThrownBy(() -> servico.processar(Pagamento.de("cli-3", MetodoDePagamento.BOLETO, "10.00")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(processados("boleto", "erro")).isEqualTo(antes + 1);
        assertThat(autorizacoes("boleto", "erro")).as("o timer também mede as falhas").isPositive();
    }

    @Test
    @DisplayName("o timer pagamentos.autorizacao mede o tempo do provedor, por metodo e resultado")
    void medeTempo() {
        when(autorizador.autorizar(any())).thenAnswer(inv -> {
            Thread.sleep(30);
            return true;
        });
        long antes = autorizacoes("pix", "aprovado");

        servico.processar(Pagamento.de("cli-4", MetodoDePagamento.PIX, "20.00"));

        Timer t = registro.get("pagamentos.autorizacao").tag("metodo", "pix").tag("resultado", "aprovado").timer();
        assertThat(t.count()).isEqualTo(antes + 1);
        assertThat(t.max(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(25);
    }

    @Test
    @DisplayName("o gauge pagamentos.em.andamento vale 1 durante a autorização e volta a 0 depois")
    void gaugeEmAndamento() {
        AtomicReference<Double> duranteAChamada = new AtomicReference<>();
        when(autorizador.autorizar(any())).thenAnswer(inv -> {
            duranteAChamada.set(registro.get("pagamentos.em.andamento").gauge().value());
            return true;
        });

        servico.processar(Pagamento.de("cli-5", MetodoDePagamento.PIX, "5.00"));

        assertThat(duranteAChamada.get()).isEqualTo(1.0);
        Gauge g = registro.get("pagamentos.em.andamento").gauge();
        assertThat(g.value()).isZero();
    }

    @Test
    @DisplayName("pagamentos.valor registra o valor dos aprovados (em reais), e não dos recusados")
    void somaValores() {
        when(autorizador.autorizar(any())).thenReturn(true);
        servico.processar(Pagamento.de("cli-6", MetodoDePagamento.CARTAO, "300.00"));
        DistributionSummary valores = registro.get("pagamentos.valor").tag("metodo", "cartao").summary();
        double total = valores.totalAmount();
        long quantos = valores.count();

        when(autorizador.autorizar(any())).thenReturn(false);
        servico.processar(Pagamento.de("cli-7", MetodoDePagamento.CARTAO, "700.00"));

        assertThat(valores.count()).isEqualTo(quantos);
        assertThat(valores.totalAmount()).isEqualTo(total);
        assertThat(valores.getId().getBaseUnit()).isEqualTo("reais");
    }

    @Test
    @DisplayName("nenhuma métrica pagamentos.* tem tag com o cliente (cardinalidade explode)")
    void semTagDeCliente() {
        when(autorizador.autorizar(any())).thenReturn(true);
        servico.processar(Pagamento.de("cliente-unico-8", MetodoDePagamento.PIX, "1.00"));

        for (Meter m : registro.getMeters()) {
            if (!m.getId().getName().startsWith("pagamentos.")) {
                continue;
            }
            assertThat(m.getId().getTags())
                    .as("tags de %s", m.getId().getName())
                    .noneMatch(t -> t.getKey().contains("cliente") || t.getValue().contains("cliente-unico-8"));
        }
    }

    @Test
    @DisplayName("/actuator/prometheus expõe o contador e os buckets do histograma")
    void prometheus() throws Exception {
        when(autorizador.autorizar(any())).thenReturn(true);
        servico.processar(Pagamento.de("cli-9", MetodoDePagamento.PIX, "42.00"));

        MvcTestResult r = mvc.get().uri("/actuator/prometheus").exchange();

        assertThat(r).hasStatus(HttpStatus.OK);
        String corpo = r.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(corpo).containsPattern("pagamentos_processados_total\\{[^}]*metodo=\"pix\"[^}]*resultado=\"aprovado\"");
        assertThat(corpo).contains("pagamentos_autorizacao_seconds_bucket{");
        assertThat(corpo).as("tag comum a todas as métricas").contains("aplicacao=\"trilha-m14\"");
    }

    @Test
    @DisplayName("só health e prometheus ficam expostos: /actuator/env não existe")
    void naoExpoeEnv() {
        assertThat(mvc.get().uri("/actuator/env").exchange()).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri("/actuator/health").exchange()).hasStatus(HttpStatus.OK);
        assertThat(mvc.get().uri("/actuator/prometheus").exchange()).hasStatus(HttpStatus.OK);
    }
}
