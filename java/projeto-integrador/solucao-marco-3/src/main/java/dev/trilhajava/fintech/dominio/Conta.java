package dev.trilhajava.fintech.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Marco 2: a conta virou entidade JPA, mas continua protegendo as próprias regras. As anotações
 * de mapeamento são o único "vazamento" de infraestrutura aceito no domínio — uma troca pragmática
 * (a alternativa, entidades de persistência separadas, dobra o código).
 *
 * A concorrência deixou de ser synchronized: quem garante é o banco (lock + CHECK saldo >= 0).
 */
@Entity
@Table(name = "conta")
public class Conta {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String titular;

    @Convert(converter = CpfConverter.class)
    @Column(nullable = false, length = 11)
    private Cpf cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoConta tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Version
    private long versao;

    protected Conta() {
    }

    public Conta(UUID id, String titular, Cpf cpf, TipoConta tipo, Instant criadaEm) {
        this.id = Objects.requireNonNull(id);
        this.titular = Objects.requireNonNull(titular);
        this.cpf = Objects.requireNonNull(cpf);
        this.tipo = Objects.requireNonNull(tipo);
        this.criadaEm = Objects.requireNonNull(criadaEm);
        this.saldo = BigDecimal.ZERO.setScale(2);
    }

    public Movimento depositar(BigDecimal valor, Instant quando) {
        BigDecimal quantia = exigirPositivo(valor);
        saldo = saldo.add(quantia);
        return new Movimento(this, TipoMovimento.DEPOSITO, quantia, saldo, quando, null);
    }

    public Movimento sacar(BigDecimal valor, Instant quando) {
        BigDecimal quantia = retirar(valor);
        return new Movimento(this, TipoMovimento.SAQUE, quantia, saldo, quando, null);
    }

    public Movimento enviar(Transferencia transferencia) {
        BigDecimal quantia = retirar(transferencia.valor());
        return new Movimento(this, TipoMovimento.TRANSFERENCIA_ENVIADA, quantia, saldo, transferencia.criadaEm(),
                transferencia.id());
    }

    public Movimento receber(Transferencia transferencia) {
        BigDecimal quantia = exigirPositivo(transferencia.valor());
        saldo = saldo.add(quantia);
        return new Movimento(this, TipoMovimento.TRANSFERENCIA_RECEBIDA, quantia, saldo, transferencia.criadaEm(),
                transferencia.id());
    }

    private BigDecimal retirar(BigDecimal valor) {
        BigDecimal quantia = exigirPositivo(valor);
        if (saldo.compareTo(quantia) < 0) {
            throw new SaldoInsuficienteException(id, saldo);
        }
        saldo = saldo.subtract(quantia);
        return quantia;
    }

    private static BigDecimal exigirPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor deve ser positivo e ter no máximo 2 casas decimais");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    public UUID id() {
        return id;
    }

    public String titular() {
        return titular;
    }

    public Cpf cpf() {
        return cpf;
    }

    public TipoConta tipo() {
        return tipo;
    }

    public Instant criadaEm() {
        return criadaEm;
    }

    public BigDecimal saldo() {
        return saldo;
    }
}
