package dev.trilhajava.m09.transferencias;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titular;

    private BigDecimal saldo;

    /**
     * Lock otimista: o UPDATE vira "... SET saldo = ?, versao = versao + 1 WHERE id = ? AND versao = ?".
     * Se outra transação alterou a linha antes, nenhuma linha é afetada e o Hibernate lança
     * uma exceção em vez de sobrescrever silenciosamente (o "lost update").
     */
    @Version
    private long versao;

    protected Conta() {
    }

    public void debitar(BigDecimal valor) {
        if (saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(id);
        }
        saldo = saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        saldo = saldo.add(valor);
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
