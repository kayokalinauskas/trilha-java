package dev.trilhajava.m10.aulas.junit;

import java.math.BigDecimal;

/** As especialidades da clínica e o valor cheio (particular) de cada consulta. */
public enum Especialidade {
    CLINICA_GERAL("180.00"),
    CARDIOLOGIA("320.00"),
    DERMATOLOGIA("245.90"),
    PEDIATRIA("210.00");

    private final BigDecimal valor;

    Especialidade(String valor) {
        this.valor = new BigDecimal(valor);
    }

    public BigDecimal valor() {
        return valor;
    }
}
