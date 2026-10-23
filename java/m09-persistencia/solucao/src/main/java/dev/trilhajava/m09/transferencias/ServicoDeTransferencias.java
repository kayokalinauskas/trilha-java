package dev.trilhajava.m09.transferencias;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeTransferencias {

    private final ContaRepository contas;

    public ServicoDeTransferencias(ContaRepository contas) {
        this.contas = contas;
    }

    /**
     * Versão OTIMISTA: não trava nada; se houver conflito, o commit falha com
     * ObjectOptimisticLockingFailureException e quem chamou decide se tenta de novo.
     */
    @Transactional
    public void transferir(long origem, long destino, BigDecimal valor) {
        validar(origem, destino, valor);
        Conta de = contas.findById(origem).orElseThrow(() -> new ContaNaoEncontradaException(origem));
        Conta para = contas.findById(destino).orElseThrow(() -> new ContaNaoEncontradaException(destino));
        de.debitar(valor);
        para.creditar(valor);
        // Sem save(): as entidades estão gerenciadas; o dirty checking gera os UPDATEs no commit.
    }

    /**
     * Versão PESSIMISTA: trava as duas contas SEMPRE NA MESMA ORDEM (menor id primeiro).
     * Sem essa ordem, A→B e B→A simultâneos travariam uma conta cada e esperariam para sempre (deadlock).
     */
    @Transactional
    public void transferirComLockPessimista(long origem, long destino, BigDecimal valor) {
        validar(origem, destino, valor);
        long primeiro = Math.min(origem, destino);
        long segundo = Math.max(origem, destino);
        Conta a = contas.buscarComLock(primeiro).orElseThrow(() -> new ContaNaoEncontradaException(primeiro));
        Conta b = contas.buscarComLock(segundo).orElseThrow(() -> new ContaNaoEncontradaException(segundo));
        Conta de = a.getId() == origem ? a : b;
        Conta para = de == a ? b : a;
        de.debitar(valor);
        para.creditar(valor);
    }

    private static void validar(long origem, long destino, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }
        if (origem == destino) {
            throw new IllegalArgumentException("Origem e destino devem ser diferentes");
        }
    }
}
