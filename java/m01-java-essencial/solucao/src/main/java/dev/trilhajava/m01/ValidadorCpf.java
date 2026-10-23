package dev.trilhajava.m01;

/** Solução de referência do exercício 01.01. */
public final class ValidadorCpf {

    private static final int TAMANHO = 11;

    private ValidadorCpf() {
    }

    public static String somenteDigitos(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder digitos = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c >= '0' && c <= '9') {
                digitos.append(c);
            }
        }
        return digitos.toString();
    }

    public static boolean ehValido(String cpf) {
        if (cpf == null || !cpf.strip().matches("\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            return false;
        }
        String digitos = somenteDigitos(cpf);
        if (todosIguais(digitos)) {
            return false;
        }
        int dv1 = calcularDigito(digitos, 9);
        int dv2 = calcularDigito(digitos, 10);
        return dv1 == valorDe(digitos, 9) && dv2 == valorDe(digitos, 10);
    }

    public static String formatar(String cpf) {
        if (!ehValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }
        String d = somenteDigitos(cpf);
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    /**
     * Calcula o dígito verificador usando os {@code quantidade} primeiros dígitos.
     * Pesos decrescem de (quantidade + 1) até 2; resto < 2 vira 0, senão 11 - resto.
     */
    private static int calcularDigito(String digitos, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += valorDe(digitos, i) * peso--;
        }
        int resto = soma % TAMANHO;
        return resto < 2 ? 0 : TAMANHO - resto;
    }

    private static int valorDe(String digitos, int posicao) {
        return digitos.charAt(posicao) - '0';
    }

    private static boolean todosIguais(String digitos) {
        for (int i = 1; i < digitos.length(); i++) {
            if (digitos.charAt(i) != digitos.charAt(0)) {
                return false;
            }
        }
        return true;
    }
}
