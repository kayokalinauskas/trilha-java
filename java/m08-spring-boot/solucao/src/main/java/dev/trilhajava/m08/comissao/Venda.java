package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;
import java.util.Objects;

/** Uma venda fechada por um vendedor em um canal (LOJA, ONLINE, PARCEIRO...). */
public record Venda(String vendedor, String canal, BigDecimal valor) {

    public Venda {
        Objects.requireNonNull(vendedor, "vendedor");
        Objects.requireNonNull(canal, "canal");
        Objects.requireNonNull(valor, "valor");
    }
}
