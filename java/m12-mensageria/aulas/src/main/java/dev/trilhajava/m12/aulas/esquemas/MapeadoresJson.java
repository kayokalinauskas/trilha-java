package dev.trilhajava.m12.aulas.esquemas;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/** Os dois mappers da aula: o tolerante (para consumir eventos) e o estrito (para comparar). */
public final class MapeadoresJson {

    private MapeadoresJson() {
    }

    /**
     * Para LER eventos de outros times. No Jackson 3, ignorar campos desconhecidos já é o padrão;
     * a linha fica explícita para documentar a decisão e sobreviver a uma mudança de padrão.
     * Valores de enum desconhecidos viram o valor marcado com {@code @JsonEnumDefaultValue}.
     */
    public static JsonMapper tolerante() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .build();
    }

    /** O comportamento do Jackson 2: falha em campo desconhecido e em valor de enum desconhecido. */
    public static JsonMapper estrito() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
