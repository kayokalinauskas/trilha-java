package dev.trilhajava.m13.aulas.decisoes.frete;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Modo sombra: o cliente recebe sempre o valor oficial; a candidata roda ao lado
 * e cada diferença (ou falha) é registrada para análise, sem afetar ninguém.
 */
public final class CalculadoraDeFreteEmSombra implements CalculadoraDeFrete {

    public record Divergencia(Remessa remessa, BigDecimal oficial, BigDecimal candidata, String erro) {
    }

    private final CalculadoraDeFrete oficial;
    private final CalculadoraDeFrete candidata;
    private final Consumer<Divergencia> registro;

    public CalculadoraDeFreteEmSombra(CalculadoraDeFrete oficial, CalculadoraDeFrete candidata,
            Consumer<Divergencia> registro) {
        this.oficial = Objects.requireNonNull(oficial, "oficial");
        this.candidata = Objects.requireNonNull(candidata, "candidata");
        this.registro = Objects.requireNonNull(registro, "registro");
    }

    @Override
    public BigDecimal calcular(Remessa remessa) {
        BigDecimal valor = oficial.calcular(remessa);
        try {
            BigDecimal sombra = candidata.calcular(remessa);
            if (sombra.compareTo(valor) != 0) {
                registro.accept(new Divergencia(remessa, valor, sombra, null));
            }
        } catch (RuntimeException falha) {
            registro.accept(new Divergencia(remessa, valor, null, falha.toString()));
        }
        return valor;
    }
}
