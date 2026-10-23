package dev.trilhajava.fintech.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Um lançamento no extrato. Sem setters: extrato não se edita, se estorna.
 * A conta NÃO tem uma coleção de movimentos: carregar milhares de lançamentos a cada depósito
 * seria caro. O extrato é uma consulta paginável no repositório.
 */
@Entity
@Table(name = "movimento")
public class Movimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimento tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_apos", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoApos;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Column(name = "transferencia_id")
    private UUID transferenciaId;

    protected Movimento() {
    }

    Movimento(Conta conta, TipoMovimento tipo, BigDecimal valor, BigDecimal saldoApos, Instant dataHora,
            UUID transferenciaId) {
        this.conta = conta;
        this.tipo = tipo;
        this.valor = valor;
        this.saldoApos = saldoApos;
        this.dataHora = dataHora;
        this.transferenciaId = transferenciaId;
    }

    public TipoMovimento tipo() {
        return tipo;
    }

    public BigDecimal valor() {
        return valor;
    }

    public BigDecimal saldoApos() {
        return saldoApos;
    }

    public Instant dataHora() {
        return dataHora;
    }

    public UUID transferenciaId() {
        return transferenciaId;
    }
}
