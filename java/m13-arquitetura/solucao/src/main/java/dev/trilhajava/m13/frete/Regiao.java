package dev.trilhajava.m13.frete;

/** Região de entrega pelo primeiro dígito do CEP (simplificação dos Correios). */
public enum Regiao {
    SUDESTE, SUL, CENTRO_OESTE, NORTE_NORDESTE;

    public static Regiao doCep(String cep) {
        return switch (cep.charAt(0)) {
            case '0', '1', '2', '3' -> SUDESTE;
            case '4', '5', '6' -> NORTE_NORDESTE;
            case '7' -> CENTRO_OESTE;
            default -> SUL;
        };
    }
}
