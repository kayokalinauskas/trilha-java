package dev.trilhajava.m14.logs;

/** Dados pessoais nunca vão inteiros para o log (LGPD). Fornecida pronta. */
public final class Mascaras {

    private Mascaras() {
    }

    /** "52998224725" → "***.***.*47-25": só os 4 últimos dígitos, que ajudam o suporte a conferir. */
    public static String cpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() < 4) {
            return "***";
        }
        String fim = digitos.substring(digitos.length() - 4);
        return "***.***.*" + fim.substring(0, 2) + "-" + fim.substring(2);
    }
}
