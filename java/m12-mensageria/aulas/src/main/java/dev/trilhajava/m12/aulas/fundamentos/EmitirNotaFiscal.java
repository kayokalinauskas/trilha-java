package dev.trilhajava.m12.aulas.fundamentos;

import java.math.BigDecimal;
import java.util.UUID;

/** Comando: uma ordem endereçada ao serviço de notas fiscais, que pode recusá-la. */
public record EmitirNotaFiscal(UUID idDoComando, String pedidoId, String cnpjDoTomador,
                               BigDecimal valor) implements Comando {
}
