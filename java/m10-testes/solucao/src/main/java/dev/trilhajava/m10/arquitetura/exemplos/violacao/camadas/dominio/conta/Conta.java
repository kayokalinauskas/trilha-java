package dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.conta;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.cliente.Cliente;
import java.math.BigDecimal;

/** Exemplo analisado pelo ArchUnit (violação: camadas). */
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
