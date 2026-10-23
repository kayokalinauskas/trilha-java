package dev.trilhajava.m13.aulas.decisoes.frete;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Escolhe, a cada chamada, a implementação antiga ou a nova, conforme a flag.
 * O mesmo cliente cai sempre no mesmo lado: o balde vem do id, não de um sorteio.
 */
public final class CalculadoraDeFreteComAlternancia implements CalculadoraDeFrete {

    public static final String FLAG = "frete-por-peso";

    private final CalculadoraDeFrete antiga;
    private final CalculadoraDeFrete nova;
    private final FeatureFlags flags;

    public CalculadoraDeFreteComAlternancia(CalculadoraDeFrete antiga, CalculadoraDeFrete nova, FeatureFlags flags) {
        this.antiga = Objects.requireNonNull(antiga, "antiga");
        this.nova = Objects.requireNonNull(nova, "nova");
        this.flags = Objects.requireNonNull(flags, "flags");
    }

    @Override
    public BigDecimal calcular(Remessa remessa) {
        return usaNova(remessa.clienteId()) ? nova.calcular(remessa) : antiga.calcular(remessa);
    }

    boolean usaNova(String clienteId) {
        int percentual = flags.percentualAtivo(FLAG);
        if (percentual <= 0) {
            return false;
        }
        if (percentual >= 100) {
            return true;
        }
        return balde(clienteId) < percentual;
    }

    /** De 0 a 99. String.hashCode tem fórmula fixa na especificação: o resultado é o mesmo em toda JVM. */
    static int balde(String clienteId) {
        return Math.floorMod(clienteId.hashCode(), 100);
    }
}
