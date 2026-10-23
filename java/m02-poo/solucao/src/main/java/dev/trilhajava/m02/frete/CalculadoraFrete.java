package dev.trilhajava.m02.frete;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Solução de referência do exercício 02.04.
 *
 * Repare que não existe "default" nos switches: se amanhã alguém criar uma nova modalidade
 * (ex.: EntregaPorDrone) e adicioná-la ao "permits", o código abaixo PARA DE COMPILAR até que
 * a nova regra seja tratada. Esse é o grande ganho de sealed + pattern matching.
 */
public class CalculadoraFrete {

    private static final BigDecimal BASE_PADRAO = new BigDecimal("15.00");
    private static final BigDecimal BASE_AGENDADA = new BigDecimal("25.00");
    private static final BigDecimal POR_KG_ADICIONAL = new BigDecimal("2.50");
    private static final BigDecimal FATOR_EXPRESSA = new BigDecimal("1.8");
    private static final BigDecimal TAXA_MESMO_DIA = new BigDecimal("20.00");
    private static final BigDecimal FRETE_GRATIS_A_PARTIR_DE = new BigDecimal("199.00");
    private static final int ANTECEDENCIA_MINIMA_DIAS = 2;
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BigDecimal calcular(Entrega entrega, BigDecimal valorPedido, LocalDate hoje) {
        if (valorPedido == null || valorPedido.signum() < 0) {
            throw new IllegalArgumentException("Valor do pedido deve ser zero ou positivo");
        }
        BigDecimal frete = switch (entrega) {
            case Retirada r -> BigDecimal.ZERO;
            case EntregaPadrao p when valorPedido.compareTo(FRETE_GRATIS_A_PARTIR_DE) >= 0 -> BigDecimal.ZERO;
            case EntregaPadrao p -> BASE_PADRAO.add(adicionalPorPeso(p.pesoKg()));
            case EntregaExpressa e -> {
                BigDecimal valor = BASE_PADRAO.add(adicionalPorPeso(e.pesoKg())).multiply(FATOR_EXPRESSA);
                yield e.mesmoDia() ? valor.add(TAXA_MESMO_DIA) : valor;
            }
            case EntregaAgendada a -> {
                validarAgendamento(a.data(), hoje);
                yield BASE_AGENDADA.add(adicionalPorPeso(a.pesoKg()));
            }
        };
        return frete.setScale(2, RoundingMode.HALF_EVEN);
    }

    public String descrever(Entrega entrega) {
        // Record patterns: desestruturam os componentes direto no case.
        return switch (entrega) {
            case Retirada(String loja) -> "Retirada na loja " + loja;
            case EntregaPadrao(Cep cep, double peso) -> "Entrega padrão para " + cep.formatado();
            case EntregaExpressa(Cep cep, double peso, boolean mesmoDia) when mesmoDia ->
                    "Entrega expressa no mesmo dia para " + cep.formatado();
            case EntregaExpressa(Cep cep, double peso, boolean mesmoDia) -> "Entrega expressa para " + cep.formatado();
            case EntregaAgendada(Cep cep, double peso, LocalDate data) ->
                    "Entrega agendada para " + data.format(DATA_BR) + " em " + cep.formatado();
        };
    }

    /** R$ 2,50 por kg acima do primeiro, contando fração como kg inteiro. */
    private static BigDecimal adicionalPorPeso(double pesoKg) {
        long kgAdicionais = (long) Math.ceil(Math.max(0, pesoKg - 1));
        return POR_KG_ADICIONAL.multiply(BigDecimal.valueOf(kgAdicionais));
    }

    private static void validarAgendamento(LocalDate data, LocalDate hoje) {
        if (data.isBefore(hoje.plusDays(ANTECEDENCIA_MINIMA_DIAS))) {
            throw new IllegalArgumentException("Agendamento exige pelo menos " + ANTECEDENCIA_MINIMA_DIAS + " dias de antecedência");
        }
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Não realizamos entregas agendadas aos domingos");
        }
    }
}
