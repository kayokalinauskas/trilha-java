package dev.trilhajava.m13.aulas.modular.cobranca;

import java.math.BigDecimal;
import java.util.UUID;

/** A operadora do plano de saúde, um sistema externo que pode estar fora do ar. */
public interface OperadoraDeConvenio {

    /** Devolve o número da autorização, ou lança exceção se a operadora estiver indisponível. */
    String autorizar(UUID consulta, BigDecimal valor);
}
