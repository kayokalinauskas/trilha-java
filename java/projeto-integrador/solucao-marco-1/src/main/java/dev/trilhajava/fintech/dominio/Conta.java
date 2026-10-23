package dev.trilhajava.fintech.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A conta protege as próprias regras: ninguém de fora altera o saldo diretamente.
 *
 * Marco 1 (em memória): os métodos são synchronized para que depósitos simultâneos na MESMA
 * conta não se percam. No marco 2 essa garantia passa a ser do banco de dados.
 */
public class Conta {

    private final UUID id;
    private final String titular;
    private final Cpf cpf;
    private final TipoConta tipo;
    private final Instant criadaEm;
    private BigDecimal saldo = BigDecimal.ZERO.setScale(2);
    private final List<Movimento> movimentos = new ArrayList<>();

    public Conta(UUID id, String titular, Cpf cpf, TipoConta tipo, Instant criadaEm) {
        this.id = Objects.requireNonNull(id);
        this.titular = Objects.requireNonNull(titular);
        this.cpf = Objects.requireNonNull(cpf);
        this.tipo = Objects.requireNonNull(tipo);
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    public synchronized Movimento depositar(BigDecimal valor, Instant quando) {
        BigDecimal quantia = exigirPositivo(valor);
        saldo = saldo.add(quantia);
        return registrar(TipoMovimento.DEPOSITO, quantia, quando);
    }

    public synchronized Movimento sacar(BigDecimal valor, Instant quando) {
        BigDecimal quantia = exigirPositivo(valor);
        if (saldo.compareTo(quantia) < 0) {
            throw new SaldoInsuficienteException(id, saldo);
        }
        saldo = saldo.subtract(quantia);
        return registrar(TipoMovimento.SAQUE, quantia, quando);
    }

    private Movimento registrar(TipoMovimento tipoMovimento, BigDecimal valor, Instant quando) {
        var movimento = new Movimento(tipoMovimento, valor, saldo, quando);
        movimentos.add(movimento);
        return movimento;
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

    public synchronized BigDecimal saldo() {
        return saldo;
    }

    public synchronized List<Movimento> movimentos() {
        return List.copyOf(movimentos);
    }
}
