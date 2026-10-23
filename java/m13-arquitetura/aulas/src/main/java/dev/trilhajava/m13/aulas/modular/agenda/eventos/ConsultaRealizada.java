package dev.trilhajava.m13.aulas.modular.agenda.eventos;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fato publicado pela agenda quando o atendimento termina. É o contrato da agenda com o resto
 * da clínica: só dados, nada de entidades internas. Mudar este record é mudar uma API pública.
 */
public record ConsultaRealizada(UUID consulta, String paciente, String procedimento, BigDecimal valor) {
}
