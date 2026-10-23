package dev.trilhajava.m14.saude;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/** Saúde do provedor de pagamentos (componente "provedor" no /actuator/health). */
@Component
public class ProvedorHealthIndicator implements HealthIndicator {

    private final ProvedorDePagamentos provedor;

    public ProvedorHealthIndicator(ProvedorDePagamentos provedor) {
        this.provedor = provedor;
    }

    @Override
    public Health health() {
        // TODO: UP com o detalhe latenciaMs; DOWN se passar de 1 s ou se o ping lançar exceção
        //  (nesse caso, detalhe "erro" com o nome simples da classe da exceção, nunca a mensagem).
        throw new UnsupportedOperationException("TODO: exercício 14.03");
    }
}
