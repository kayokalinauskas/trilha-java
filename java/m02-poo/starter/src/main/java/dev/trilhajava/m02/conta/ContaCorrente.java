package dev.trilhajava.m02.conta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exercício 02.01 — Conta corrente com cheque especial.
 * Rode: mvn test -Dtest=ContaCorrenteTest
 *
 * Regras resumidas (detalhes no enunciado do site):
 * - titular obrigatório; limite de cheque especial >= 0
 * - valores de depósito/saque/transferência devem ser > 0
 * - saldo pode ficar negativo até -limite; além disso, SaldoInsuficienteException
 * - extrato não pode ser alterado por quem o recebe
 */
public class ContaCorrente {

    // TODO: declare os atributos. Pense: quais podem ser final? Quem pode alterá-los?

    public ContaCorrente(String titular, BigDecimal limiteChequeEspecial) {
        throw new UnsupportedOperationException("TODO: valide e inicialize a conta");
    }

    public void depositar(BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }

    public void sacar(BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }

    public void transferirPara(ContaCorrente destino, BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }

    public String getTitular() {
        throw new UnsupportedOperationException("TODO");
    }

    public BigDecimal getSaldo() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Saldo + limite do cheque especial. */
    public BigDecimal getSaldoDisponivel() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Movimentacao> extrato() {
        throw new UnsupportedOperationException("TODO");
    }
}
