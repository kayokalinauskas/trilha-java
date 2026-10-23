package dev.trilhajava.m02.dinheiro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Solução de referência do exercício 02.03.
 *
 * Por que normalizar a escala no construtor? Porque BigDecimal.equals compara valor E escala:
 * new BigDecimal("10").equals(new BigDecimal("10.00")) é false. Normalizando na entrada,
 * o equals gerado pelo record passa a funcionar como o negócio espera.
 */
public record Dinheiro(BigDecimal valor, Moeda moeda) {

    private static final int CASAS = 2;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    public Dinheiro {
        if (valor == null) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }
        if (moeda == null) {
            throw new IllegalArgumentException("Moeda é obrigatória");
        }
        valor = valor.setScale(CASAS, ARREDONDAMENTO);
    }

    public static Dinheiro de(String valor, Moeda moeda) {
        return new Dinheiro(new BigDecimal(valor), moeda);
    }

    public static Dinheiro reais(String valor) {
        return de(valor, Moeda.BRL);
    }

    public static Dinheiro zero(Moeda moeda) {
        return new Dinheiro(BigDecimal.ZERO, moeda);
    }

    public Dinheiro somar(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        return new Dinheiro(valor.add(outro.valor), moeda);
    }

    public Dinheiro subtrair(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        return new Dinheiro(valor.subtract(outro.valor), moeda);
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        if (fator == null) {
            throw new IllegalArgumentException("Fator é obrigatório");
        }
        return new Dinheiro(valor.multiply(fator), moeda); // o construtor arredonda
    }

    public boolean ehNegativo() {
        return valor.signum() < 0;
    }

    public boolean ehZero() {
        return valor.signum() == 0;
    }

    public List<Dinheiro> ratear(int partes) {
        if (partes < 1) {
            throw new IllegalArgumentException("Número de partes deve ser pelo menos 1");
        }
        long centavos = valor.movePointRight(CASAS).longValueExact();
        long base = centavos / partes;
        long resto = centavos % partes;
        List<Dinheiro> resultado = new ArrayList<>(partes);
        for (int i = 0; i < partes; i++) {
            long parte = base + (i < resto ? 1 : 0); // distribui 1 centavo para cada uma das primeiras "resto" partes
            resultado.add(new Dinheiro(BigDecimal.valueOf(parte, CASAS), moeda));
        }
        return List.copyOf(resultado);
    }

    public String formatado() {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));
        String numero = formato.format(valor.abs());
        return (ehNegativo() ? "-" : "") + moeda.simbolo() + " " + numero;
    }

    private void exigirMesmaMoeda(Dinheiro outro) {
        if (outro == null) {
            throw new IllegalArgumentException("Valor a operar é obrigatório");
        }
        if (outro.moeda != moeda) {
            throw new IllegalArgumentException("Moedas diferentes: " + moeda + " e " + outro.moeda);
        }
    }
}
