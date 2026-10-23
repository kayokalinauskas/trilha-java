package dev.trilhajava.m13.aulas.modular.cobranca;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Simulação da operadora para a aula. Package-private: só o módulo cobranca a enxerga. */
@Component
class OperadoraSimulada implements OperadoraDeConvenio {

    @Override
    public String autorizar(UUID consulta, BigDecimal valor) {
        return "AUT-" + consulta.toString().substring(0, 8).toUpperCase();
    }
}
