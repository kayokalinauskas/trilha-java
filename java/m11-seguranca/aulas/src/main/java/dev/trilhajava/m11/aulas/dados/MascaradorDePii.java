package dev.trilhajava.m11.aulas.dados;

/**
 * Mascaramento de dados pessoais para logs, traces e telas de suporte. Mascarar é irreversível e
 * serve para EXIBIR: quem lê o log reconhece "o CPF terminado em 09" sem conhecer o número.
 */
public final class MascaradorDePii {

    private MascaradorDePii() {
    }

    /** "12345678909" vira "***.***.***-09". */
    public static String cpf(String digitos) {
        return "***.***.***-" + digitos.substring(digitos.length() - 2);
    }

    /** "marina.souza@clinica.com.br" vira "m***@clinica.com.br". */
    public static String email(String email) {
        int arroba = email.indexOf('@');
        if (arroba < 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(arroba);
    }

    /** "+55 11 98765-4321" vira "(**) *****-4321": sobram só os 4 últimos dígitos. */
    public static String telefone(String telefone) {
        StringBuilder digitos = new StringBuilder();
        for (int i = 0; i < telefone.length(); i++) {
            if (Character.isDigit(telefone.charAt(i))) {
                digitos.append(telefone.charAt(i));
            }
        }
        if (digitos.length() < 8) {
            return "***";
        }
        return "(**) *****-" + digitos.substring(digitos.length() - 4);
    }

    /**
     * Rede de segurança para texto livre (mensagens de exceção, campos de observação): troca
     * qualquer coisa com cara de CPF, com ou sem pontuação, pela versão mascarada. Não substitui
     * o cuidado de não logar o dado; só pega o que escapou.
     */
    public static String mascararCpfsNoTexto(String texto) {
        StringBuilder saida = new StringBuilder(texto.length());
        int i = 0;
        while (i < texto.length()) {
            boolean inicioDeNumero = Character.isDigit(texto.charAt(i))
                    && (i == 0 || !Character.isDigit(texto.charAt(i - 1)));
            int tamanho = inicioDeNumero ? tamanhoDoCpfEm(texto, i) : 0;
            if (tamanho > 0) {
                String digitos = texto.substring(i, i + tamanho).replace(".", "").replace("-", "");
                saida.append(cpf(digitos));
                i += tamanho;
            } else {
                saida.append(texto.charAt(i));
                i++;
            }
        }
        return saida.toString();
    }

    /** 14 para "123.456.789-09", 11 para "12345678909", 0 se não for um CPF nessa posição. */
    private static int tamanhoDoCpfEm(String texto, int inicio) {
        String formatado = "ddd.ddd.ddd-dd";
        if (casa(texto, inicio, formatado)) {
            return formatado.length();
        }
        if (casa(texto, inicio, "ddddddddddd")) {
            return 11;
        }
        return 0;
    }

    private static boolean casa(String texto, int inicio, String molde) {
        int fim = inicio + molde.length();
        if (fim > texto.length() || (fim < texto.length() && Character.isDigit(texto.charAt(fim)))) {
            return false;
        }
        for (int j = 0; j < molde.length(); j++) {
            char esperado = molde.charAt(j);
            char atual = texto.charAt(inicio + j);
            boolean ok = esperado == 'd' ? Character.isDigit(atual) : atual == esperado;
            if (!ok) {
                return false;
            }
        }
        return true;
    }
}
