package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Movimento;
import dev.trilhajava.fintech.dominio.TipoMovimento;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExtratoResponse(UUID contaId, BigDecimal saldo, List<Item> movimentos) {

    public record Item(TipoMovimento tipo, BigDecimal valor, BigDecimal saldoApos, Instant dataHora) {

        static Item de(Movimento m) {
            return new Item(m.tipo(), m.valor(), m.saldoApos(), m.dataHora());
        }
    }

    static ExtratoResponse de(Conta conta) {
        return new ExtratoResponse(conta.id(), conta.saldo(), conta.movimentos().stream().map(Item::de).toList());
    }
}
