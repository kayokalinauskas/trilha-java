package dev.trilhajava.m02.frete;

/** CEP brasileiro: sempre guardado como 8 dígitos. */
public record Cep(String digitos) {

    public Cep {
        // TODO: aceite "01310-100" ou "01310100" (com espaços nas pontas), guarde só os 8 dígitos.
        // Qualquer outra coisa: IllegalArgumentException.
    }

    /** Ex.: "01310-100". */
    public String formatado() {
        throw new UnsupportedOperationException("TODO");
    }
}
