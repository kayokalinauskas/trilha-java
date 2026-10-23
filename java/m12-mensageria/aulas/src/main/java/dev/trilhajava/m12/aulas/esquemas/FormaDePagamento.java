package dev.trilhajava.m12.aulas.esquemas;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Forma de pagamento, como o consumidor a enxerga.
 *
 * <p>O produtor pode criar valores novos a qualquer momento (amanhã pode surgir
 * {@code PIX_PARCELADO}). Com {@code EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE}
 * ligado no mapper, um valor que este código não conhece vira {@link #DESCONHECIDA}
 * em vez de derrubar a desserialização do evento inteiro.
 */
public enum FormaDePagamento {
    CARTAO_DE_CREDITO,
    BOLETO,
    PIX,
    @JsonEnumDefaultValue
    DESCONHECIDA
}
