package dev.trilhajava.m01;

/**
 * Exercício 01.03 — Relatório da estação meteorológica.
 * Rode: mvn test -Dtest=EstacaoMeteorologicaTest
 *
 * Regra geral: arrays nulos ou vazios lançam IllegalArgumentException. Nunca altere o array recebido.
 */
public final class EstacaoMeteorologica {

    private EstacaoMeteorologica() {
    }

    public static double minima(double[] leituras) {
        throw new UnsupportedOperationException("TODO: implemente minima");
    }

    public static double maxima(double[] leituras) {
        throw new UnsupportedOperationException("TODO: implemente maxima");
    }

    public static double media(double[] leituras) {
        throw new UnsupportedOperationException("TODO: implemente media");
    }

    /** Tamanho da maior sequência de leituras consecutivas estritamente crescentes (mínimo 1). */
    public static int maiorSequenciaDeAlta(double[] leituras) {
        throw new UnsupportedOperationException("TODO: implemente maiorSequenciaDeAlta");
    }

    /** Quantas leituras são estritamente maiores que o limite. */
    public static int contarAcimaDe(double[] leituras, double limite) {
        throw new UnsupportedOperationException("TODO: implemente contarAcimaDe");
    }

    /** Relatório em 5 linhas — veja o formato exato no teste. Use Locale.US para o ponto decimal. */
    public static String formatarRelatorio(double[] leituras) {
        throw new UnsupportedOperationException("TODO: implemente formatarRelatorio");
    }
}
