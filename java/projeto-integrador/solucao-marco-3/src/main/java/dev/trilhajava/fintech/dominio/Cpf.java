package dev.trilhajava.fintech.dominio;

/** Value object: um CPF só existe se for válido. Fora do domínio, ele só aparece mascarado. */
public record Cpf(String numero) {

    public Cpf {
        if (!valido(numero)) {
            throw new IllegalArgumentException("CPF inválido");
        }
    }

    public static boolean valido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cpf, 9) == cpf.charAt(9) - '0' && digito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public String mascarado() {
        return "***." + numero.substring(3, 6) + "." + numero.substring(6, 9) + "-**";
    }

    @Override
    public String toString() {
        return mascarado(); // nunca vaza o CPF inteiro em logs por acidente
    }
}
