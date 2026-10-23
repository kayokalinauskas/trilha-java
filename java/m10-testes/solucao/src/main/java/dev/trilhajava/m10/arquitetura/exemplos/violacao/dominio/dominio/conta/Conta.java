package dev.trilhajava.m10.arquitetura.exemplos.violacao.dominio.dominio.conta;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.dominio.dominio.cliente.Cliente;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Exemplo analisado pelo ArchUnit (violação: dominio). */
@Component
public class Conta {

    private final long id;
    private final Cliente titular;
    private BigDecimal saldo = BigDecimal.ZERO;

    public Conta(long id, Cliente titular) {
        this.id = id;
        this.titular = titular;
    }

    public void depositar(BigDecimal valor) {
        saldo = saldo.add(valor);
    }

    public long id() {
        return id;
    }

    public Cliente titular() {
        return titular;
    }

    public BigDecimal saldo() {
        return saldo;
    }
}
