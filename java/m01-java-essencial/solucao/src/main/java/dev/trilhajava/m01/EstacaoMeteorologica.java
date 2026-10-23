package dev.trilhajava.m01;

import java.util.Locale;

/** Solução de referência do exercício 01.03. */
public final class EstacaoMeteorologica {

    private EstacaoMeteorologica() {
    }

    public static double minima(double[] leituras) {
        exigirLeituras(leituras);
        double menor = leituras[0];
        for (double t : leituras) {
            menor = Math.min(menor, t);
        }
        return menor;
    }

    public static double maxima(double[] leituras) {
        exigirLeituras(leituras);
        double maior = leituras[0];
        for (double t : leituras) {
            maior = Math.max(maior, t);
        }
        return maior;
    }

    public static double media(double[] leituras) {
        exigirLeituras(leituras);
        double soma = 0;
        for (double t : leituras) {
            soma += t;
        }
        return soma / leituras.length;
    }

    public static int maiorSequenciaDeAlta(double[] leituras) {
        exigirLeituras(leituras);
        int maior = 1;
        int atual = 1;
        for (int i = 1; i < leituras.length; i++) {
            atual = leituras[i] > leituras[i - 1] ? atual + 1 : 1;
            maior = Math.max(maior, atual);
        }
        return maior;
    }

    public static int contarAcimaDe(double[] leituras, double limite) {
        exigirLeituras(leituras);
        int total = 0;
        for (double t : leituras) {
            if (t > limite) {
                total++;
            }
        }
        return total;
    }

    public static String formatarRelatorio(double[] leituras) {
        exigirLeituras(leituras);
        return String.format(Locale.US, """
                Leituras: %d
                Mínima: %.1f °C
                Máxima: %.1f °C
                Média: %.1f °C
                Maior sequência de alta: %d leituras""",
                leituras.length, minima(leituras), maxima(leituras), media(leituras), maiorSequenciaDeAlta(leituras));
    }

    private static void exigirLeituras(double[] leituras) {
        if (leituras == null || leituras.length == 0) {
            throw new IllegalArgumentException("É preciso ao menos uma leitura");
        }
    }
}
