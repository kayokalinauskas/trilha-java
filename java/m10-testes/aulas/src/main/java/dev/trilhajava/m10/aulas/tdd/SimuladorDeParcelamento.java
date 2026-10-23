package dev.trilhajava.m10.aulas.tdd;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Simula o parcelamento de uma compra no cartão. Construída com TDD na aula 10.06.
 *
 * <ul>
 *   <li>de 1 a 12 parcelas;</li>
 *   <li>os centavos que sobram da divisão vão para a primeira parcela (a soma é sempre o total);</li>
 *   <li>com mais de uma parcela, cada uma tem no mínimo R$ 5,00;</li>
 *   <li>a parcela N vence N meses depois da compra (compra no dia 31 vence no último dia dos meses curtos).</li>
 * </ul>
 */
public class SimuladorDeParcelamento {

    private static final int MAXIMO_DE_PARCELAS = 12;
    private static final long PARCELA_MINIMA_EM_CENTAVOS = 500;

    private final Clock relogio;

    public SimuladorDeParcelamento(Clock relogio) {
        this.relogio = relogio;
    }

    public List<Parcela> simular(BigDecimal total, int quantidade) {
        if (quantidade < 1 || quantidade > MAXIMO_DE_PARCELAS) {
            throw new IllegalArgumentException("parcelas devem ser de 1 a 12, mas foram " + quantidade);
        }
        long centavos = emCentavos(total);
        long valorBase = centavos / quantidade;
        long sobra = centavos % quantidade;
        if (quantidade > 1 && valorBase < PARCELA_MINIMA_EM_CENTAVOS) {
            throw new IllegalArgumentException("cada parcela deve ter no mínimo R$ 5,00");
        }
        LocalDate dataDaCompra = LocalDate.now(relogio);

        List<Parcela> parcelas = new ArrayList<>(quantidade);
        for (int numero = 1; numero <= quantidade; numero++) {
            long valor = numero == 1 ? valorBase + sobra : valorBase;
            parcelas.add(new Parcela(numero, emReais(valor), dataDaCompra.plusMonths(numero)));
        }
        return List.copyOf(parcelas);
    }

    private static long emCentavos(BigDecimal valor) {
        return valor.movePointRight(2).longValueExact();
    }

    private static BigDecimal emReais(long centavos) {
        return BigDecimal.valueOf(centavos, 2);
    }
}
