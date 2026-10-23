package dev.trilhajava.m01;

import java.text.Normalizer;
import java.util.Locale;

/** Solução de referência do exercício 01.04. */
public final class GeradorSlug {

    public static final int TAMANHO_MAXIMO = 60;

    private GeradorSlug() {
    }

    public static String gerar(String titulo) {
        if (titulo == null) {
            throw new IllegalArgumentException("O título não pode ser nulo");
        }
        String slug = juntarComHifens(removerAcentos(titulo).toLowerCase(Locale.ROOT));
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("O título não contém letras nem números: \"" + titulo + "\"");
        }
        return limitar(slug);
    }

    /** Regra 2: "á" vira "a" + acento combinante (NFD); depois os acentos soltos são descartados. */
    private static String removerAcentos(String texto) {
        String decomposto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        StringBuilder semAcento = new StringBuilder(decomposto.length());
        for (char c : decomposto.toCharArray()) {
            if (Character.getType(c) != Character.NON_SPACING_MARK) {
                semAcento.append(c);
            }
        }
        return semAcento.toString();
    }

    /** Regras 3 e 4: letras e dígitos entram; qualquer sequência de outros caracteres vira um único hífen. */
    private static String juntarComHifens(String texto) {
        StringBuilder slug = new StringBuilder(texto.length());
        boolean hifenPendente = false;
        for (char c : texto.toCharArray()) {
            boolean valido = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
            if (!valido) {
                hifenPendente = true;
                continue;
            }
            // O hífen só entra ENTRE dois trechos válidos: nunca no começo nem no fim.
            if (hifenPendente && !slug.isEmpty()) {
                slug.append('-');
            }
            hifenPendente = false;
            slug.append(c);
        }
        return slug.toString();
    }

    /** Regra 5: o maior prefixo de palavras inteiras que cabe no limite; uma palavra gigante é cortada. */
    private static String limitar(String slug) {
        if (slug.length() <= TAMANHO_MAXIMO) {
            return slug;
        }
        // Se o caractere logo depois do limite é um hífen, a palavra terminou exatamente em 60: cabe inteira.
        if (slug.charAt(TAMANHO_MAXIMO) == '-') {
            return slug.substring(0, TAMANHO_MAXIMO);
        }
        int ultimoHifen = slug.lastIndexOf('-', TAMANHO_MAXIMO - 1);
        return ultimoHifen > 0 ? slug.substring(0, ultimoHifen) : slug.substring(0, TAMANHO_MAXIMO);
    }
}
