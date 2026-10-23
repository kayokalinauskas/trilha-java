package dev.trilhajava.m02.frete;

/** CEP brasileiro, sempre guardado como 8 dígitos. Dois CEPs iguais são equals — de graça, pelo record. */
public record Cep(String digitos) {

    public Cep {
        if (digitos == null) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }
        String limpo = digitos.strip();
        if (!limpo.matches("[0-9]{5}-?[0-9]{3}")) {
            throw new IllegalArgumentException("CEP inválido: \"" + digitos + "\"");
        }
        digitos = limpo.replace("-", "");
    }

    public String formatado() {
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }
}
