package dev.trilhajava.m14.metricas;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Processa pagamentos e publica as métricas de negócio que o time precisa ver num painel:
 * quantos, de que tipo, com que resultado, quanto tempo leva e quanto dinheiro passa.
 */
@Service
public class ServicoDePagamentos {

    public enum Resultado { APROVADO, RECUSADO }

    private final Autorizador autorizador;
    private final MeterRegistry metricas;
    private final AtomicInteger emAndamento;

    public ServicoDePagamentos(Autorizador autorizador, MeterRegistry metricas) {
        this.autorizador = autorizador;
        this.metricas = metricas;
        // Gauge: o registro guarda uma referência FRACA ao objeto; mantenha-o num campo.
        this.emAndamento = metricas.gauge("pagamentos.em.andamento", new AtomicInteger());
    }

    public Resultado processar(Pagamento pagamento) {
        String metodo = pagamento.metodo().name().toLowerCase(Locale.ROOT);
        Timer.Sample cronometro = Timer.start(metricas);
        emAndamento.incrementAndGet();
        String resultado = "erro";
        try {
            boolean aprovado = autorizador.autorizar(pagamento);
            resultado = aprovado ? "aprovado" : "recusado";
            if (aprovado) {
                valores(metodo).record(pagamento.valor().doubleValue());
            }
            return aprovado ? Resultado.APROVADO : Resultado.RECUSADO;
        } finally {
            emAndamento.decrementAndGet();
            cronometro.stop(duracao(metodo, resultado));
            // Tags com poucos valores possíveis: método (3) × resultado (3). NUNCA o id do cliente.
            Counter.builder("pagamentos.processados")
                    .description("Pagamentos processados, por método e resultado")
                    .tag("metodo", metodo)
                    .tag("resultado", resultado)
                    .register(metricas)
                    .increment();
        }
    }

    private Timer duracao(String metodo, String resultado) {
        return Timer.builder("pagamentos.autorizacao")
                .description("Tempo de autorização no provedor")
                .tag("metodo", metodo)
                .tag("resultado", resultado)
                .publishPercentileHistogram()   // buckets no Prometheus: p95/p99 calculados no servidor
                .register(metricas);
    }

    private DistributionSummary valores(String metodo) {
        return DistributionSummary.builder("pagamentos.valor")
                .description("Valor dos pagamentos aprovados")
                .baseUnit("reais")
                .tag("metodo", metodo)
                .register(metricas);
    }
}
