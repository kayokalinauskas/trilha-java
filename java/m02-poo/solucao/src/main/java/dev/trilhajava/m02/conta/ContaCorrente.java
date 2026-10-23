package dev.trilhajava.m02.conta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Solução de referência do exercício 02.01.
 *
 * Ideias centrais:
 * - a conta protege a própria invariante (saldo >= -limite): ninguém de fora altera o saldo diretamente;
 * - não existem setters: o estado muda só por operações com significado de negócio;
 * - o extrato é devolvido como cópia imutável, então quem chama não consegue "reescrever o passado".
 */
public class ContaCorrente {

    private final String titular;
    private final BigDecimal limiteChequeEspecial;
    private final List<Movimentacao> movimentacoes = new ArrayList<>();
    private final Clock relogio;
    private BigDecimal saldo = BigDecimal.ZERO;

    public ContaCorrente(String titular, BigDecimal limiteChequeEspecial) {
        this(titular, limiteChequeEspecial, Clock.systemUTC());
    }

    /** Construtor que recebe o relógio: facilita testar datas sem depender da hora real. */
    ContaCorrente(String titular, BigDecimal limiteChequeEspecial, Clock relogio) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException("Titular é obrigatório");
        }
        if (limiteChequeEspecial == null || limiteChequeEspecial.signum() < 0) {
            throw new IllegalArgumentException("Limite do cheque especial deve ser zero ou positivo");
        }
        this.titular = titular.strip();
        this.limiteChequeEspecial = limiteChequeEspecial;
        this.relogio = relogio;
    }

    public void depositar(BigDecimal valor) {
        exigirPositivo(valor);
        creditar(valor, TipoMovimentacao.CREDITO);
    }

    public void sacar(BigDecimal valor) {
        exigirPositivo(valor);
        debitar(valor, TipoMovimentacao.DEBITO);
    }

    public void transferirPara(ContaCorrente destino, BigDecimal valor) {
        if (destino == null) {
            throw new IllegalArgumentException("Conta de destino é obrigatória");
        }
        if (destino == this) {
            throw new IllegalArgumentException("Não é possível transferir para a própria conta");
        }
        exigirPositivo(valor);
        // Debita primeiro: se faltar saldo, a exceção sai daqui e o destino nunca é tocado.
        debitar(valor, TipoMovimentacao.TRANSFERENCIA_ENVIADA);
        destino.creditar(valor, TipoMovimentacao.TRANSFERENCIA_RECEBIDA);
    }

    public String getTitular() {
        return titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public BigDecimal getSaldoDisponivel() {
        return saldo.add(limiteChequeEspecial);
    }

    public List<Movimentacao> extrato() {
        return List.copyOf(movimentacoes);
    }

    private void creditar(BigDecimal valor, TipoMovimentacao tipo) {
        saldo = saldo.add(valor);
        registrar(tipo, valor);
    }

    private void debitar(BigDecimal valor, TipoMovimentacao tipo) {
        if (valor.compareTo(getSaldoDisponivel()) > 0) {
            throw new SaldoInsuficienteException(valor, getSaldoDisponivel());
        }
        saldo = saldo.subtract(valor);
        registrar(tipo, valor);
    }

    private void registrar(TipoMovimentacao tipo, BigDecimal valor) {
        movimentacoes.add(new Movimentacao(tipo, valor, saldo, Instant.now(relogio)));
    }

    private static void exigirPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
    }
}
