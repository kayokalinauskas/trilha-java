package dev.trilhajava.m11.aulas.dados;

/**
 * Um CPF já normalizado (só os 11 dígitos). O {@code toString()} é mascarado de propósito: se
 * alguém escrever {@code log.info("cadastro de {}", cpf)}, o log recebe {@code ***.***.***-09},
 * nunca o número inteiro. Para obter os dígitos, é preciso pedir explicitamente {@link #digitos()}.
 */
public record Cpf(String digitos) {

    public Cpf {
        StringBuilder limpo = new StringBuilder(11);
        for (int i = 0; i < digitos.length(); i++) {
            char c = digitos.charAt(i);
            if (Character.isDigit(c)) {
                limpo.append(c);
            } else if (c != '.' && c != '-' && c != ' ') {
                throw new IllegalArgumentException("CPF com caractere inválido");
            }
        }
        if (limpo.length() != 11) {
            throw new IllegalArgumentException("CPF precisa ter 11 dígitos");
        }
        digitos = limpo.toString();
    }

    @Override
    public String toString() {
        return MascaradorDePii.cpf(digitos);
    }
}
