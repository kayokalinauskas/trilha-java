package dev.trilhajava.m14.metricas;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Processa pagamentos e publica as métricas de negócio. Os nomes, as tags e o tipo
 * de cada métrica estão no enunciado.
 */
@Service
public class ServicoDePagamentos {

    public enum Resultado { APROVADO, RECUSADO }

    private final Autorizador autorizador;
    private final MeterRegistry metricas;

    public ServicoDePagamentos(Autorizador autorizador, MeterRegistry metricas) {
        this.autorizador = autorizador;
        this.metricas = metricas;
    }

    public Resultado processar(Pagamento pagamento) {
        // TODO: chame o autorizador e registre pagamentos.processados, pagamentos.autorizacao,
        //  pagamentos.em.andamento e pagamentos.valor. Exceções do autorizador sobem, mas contam.
        throw new UnsupportedOperationException("TODO: exercício 14.02");
    }
}
