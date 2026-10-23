package dev.trilhajava.m16.seguranca;

import java.util.regex.Pattern;

/**
 * Troca dados pessoais por marcadores antes de o texto sair da empresa (LGPD: minimização).
 * A ordem importa: telefone antes de cartão (senão "+55 11 98765-4321" parece um cartão de 13 dígitos)
 * e CPF antes de cartão.
 */
public final class MascaradorDePii {

    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");
    private static final Pattern TELEFONE = Pattern.compile("(\\+55\\s?)?(?<!\\d)\\(?\\d{2}\\)?\\s?9?\\d{4}-\\d{4}(?!\\d)");
    private static final Pattern CPF = Pattern.compile("(?<!\\d)(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})(?!\\d)");
    private static final Pattern CARTAO = Pattern.compile("(?<!\\d)(\\d[ -]?){12,18}\\d(?!\\d)");

    private MascaradorDePii() {
    }

    public static String mascarar(String texto) {
        if (texto == null) {
            return null;
        }
        String resultado = EMAIL.matcher(texto).replaceAll("[EMAIL]");
        resultado = TELEFONE.matcher(resultado).replaceAll("[TELEFONE]");
        resultado = CPF.matcher(resultado).replaceAll("[CPF]");
        return CARTAO.matcher(resultado).replaceAll("[CARTAO]");
    }
}
