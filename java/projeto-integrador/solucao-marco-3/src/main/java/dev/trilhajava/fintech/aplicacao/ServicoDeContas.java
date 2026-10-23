package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.ContaDuplicadaException;
import dev.trilhajava.fintech.dominio.ContaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.Movimento;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeMovimentos;
import dev.trilhajava.fintech.dominio.TipoConta;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de contas. Cada método público é uma transação. */
@Service
public class ServicoDeContas {

    private final RepositorioDeContas contas;
    private final RepositorioDeMovimentos movimentos;
    private final Clock clock;

    public ServicoDeContas(RepositorioDeContas contas, RepositorioDeMovimentos movimentos, Clock clock) {
        this.contas = contas;
        this.movimentos = movimentos;
        this.clock = clock;
    }

    @Transactional
    public Conta abrir(String titular, Cpf cpf, TipoConta tipo) {
        if (contas.existe(cpf, tipo)) {
            throw new ContaDuplicadaException(tipo);
        }
        var conta = new Conta(UUID.randomUUID(), titular.strip(), cpf, tipo, clock.instant());
        try {
            contas.salvar(conta);
        } catch (DataIntegrityViolationException corrida) {
            // Duas aberturas simultâneas passaram pelo "existe": a constraint UNIQUE decide.
            throw new ContaDuplicadaException(tipo);
        }
        return conta;
    }

    @Transactional(readOnly = true)
    public Conta buscar(UUID id) {
        return contas.buscar(id).orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public Pagina<Conta> listar(Cpf cpf, int pagina, int tamanho) {
        return new Pagina<>(contas.listar(cpf, pagina, tamanho), pagina, tamanho, contas.contar(cpf));
    }

    @Transactional
    public Conta depositar(UUID id, BigDecimal valor) {
        Conta conta = travar(id);
        movimentos.registrar(conta.depositar(valor, clock.instant()));
        return conta;
    }

    @Transactional
    public Conta sacar(UUID id, BigDecimal valor) {
        Conta conta = travar(id);
        movimentos.registrar(conta.sacar(valor, clock.instant()));
        return conta;
    }

    @Transactional(readOnly = true)
    public Extrato extrato(UUID id) {
        Conta conta = buscar(id);
        List<Movimento> lancamentos = movimentos.extrato(id);
        return new Extrato(conta, lancamentos);
    }

    /** Depósitos e saques simultâneos na mesma conta passam a ser feitos um de cada vez. */
    private Conta travar(UUID id) {
        return contas.buscarParaAtualizar(id).orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    public record Extrato(Conta conta, List<Movimento> movimentos) {
    }
}
