package dev.trilhajava.m11.aulas.owasp.logs;

/**
 * Prepara texto vindo de fora (cabeçalhos, e-mail digitado, nome de arquivo) para ir a um log de texto.
 * Quebras de linha e outros caracteres de controle viram sequências visíveis, então ninguém consegue
 * forjar uma linha nova no arquivo de log (CWE-117). O texto também é cortado num tamanho máximo.
 */
public final class NeutralizadorDeLog {

    static final int TAMANHO_MAXIMO = 200;

    private NeutralizadorDeLog() {
    }

    public static String limpar(String valor) {
        if (valor == null) {
            return "null";
        }
        var saida = new StringBuilder(Math.min(valor.length(), TAMANHO_MAXIMO) + 16);
        for (int i = 0; i < valor.length() && i < TAMANHO_MAXIMO; i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '\r' -> saida.append("\\r");
                case '\n' -> saida.append("\\n");
                case '\t' -> saida.append("\\t");
                default -> {
                    if (Character.isISOControl(c) || c == ' ' || c == ' ') {
                        saida.append(String.format("\\u%04x", (int) c));
                    } else {
                        saida.append(c);
                    }
                }
            }
        }
        if (valor.length() > TAMANHO_MAXIMO) {
            saida.append("…(+").append(valor.length() - TAMANHO_MAXIMO).append(')');
        }
        return saida.toString();
    }
}
