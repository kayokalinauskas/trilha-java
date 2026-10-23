package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.TipoConta;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContaResponse(UUID id, String titular, String cpf, TipoConta tipo, BigDecimal saldo, Instant criadaEm) {

    static ContaResponse de(Conta conta) {
        return new ContaResponse(conta.id(), conta.titular(), conta.cpf().mascarado(), conta.tipo(), conta.saldo(),
                conta.criadaEm());
    }
}
