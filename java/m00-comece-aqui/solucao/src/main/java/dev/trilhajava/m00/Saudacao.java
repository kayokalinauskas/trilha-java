package dev.trilhajava.m00;

import java.util.Locale;

/** Solução de referência do exercício 00.01. */
public final class Saudacao {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Saudacao() {
    }

    public static String para(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Olá, visitante!";
        }
        return "Olá, " + capitalizar(nome.strip()) + "!";
    }

    private static String capitalizar(String nome) {
        String[] partes = nome.toLowerCase(PT_BR).split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String parte : partes) {
            if (!resultado.isEmpty()) {
                resultado.append(' ');
            }
            resultado.append(parte.substring(0, 1).toUpperCase(PT_BR)).append(parte.substring(1));
        }
        return resultado.toString();
    }
}
