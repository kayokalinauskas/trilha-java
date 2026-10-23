package dev.trilhajava.m13.aulas.resiliencia;

import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.ConcurrencyLimit.ThrottlePolicy;

/**
 * Bulkhead: no máximo 4 consultas à operadora ao mesmo tempo. Se ela travar, trava só 4 threads;
 * as outras continuam atendendo agenda, prontuário e login.
 * REJECT recusa na hora quem passar do limite (InvocationRejectedException), em vez de enfileirar.
 */
public class ConsultaProtegidaDeElegibilidade {

    private final VerificadorDeElegibilidade verificador;

    public ConsultaProtegidaDeElegibilidade(VerificadorDeElegibilidade verificador) {
        this.verificador = verificador;
    }

    @ConcurrencyLimit(limit = 4, policy = ThrottlePolicy.REJECT)
    public Elegibilidade consultar(String carteirinha) {
        return verificador.verificar(carteirinha);
    }
}
