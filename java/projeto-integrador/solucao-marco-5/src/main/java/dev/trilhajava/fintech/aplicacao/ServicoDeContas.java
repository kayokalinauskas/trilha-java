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

/**
 * Casos de uso de contas. Marco 4: toda operação recebe o {@link Solicitante}. A conta de outra pessoa
 * é tratada como INEXISTENTE (404) — responder 403 confirmaria que o id existe.
 */
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
    public Conta abrir(Solicitante quem, String titular, Cpf cpf, TipoConta tipo) {
        if (contas.existe(cpf, tipo)) {
            throw new ContaDuplicadaException(tipo);
        }
        var conta = new Conta(UUID.randomUUID(), titular.strip(), cpf, tipo, clock.instant(), quem.usuario());
        try {
            contas.salvar(conta);
        } catch (DataIntegrityViolationException corrida) {
            // Duas aberturas simultâneas passaram pelo "existe": a constraint UNIQUE decide.
            throw new ContaDuplicadaException(tipo);
        }
        return conta;
    }

    @Transactional(readOnly = true)
    public Conta buscar(Solicitante quem, UUID id) {
        return contas.buscar(id).filter(quem::podeAcessar).orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    /** O cliente lista só as próprias contas; o ADMIN, todas. */
    @Transactional(readOnly = true)
    public Pagina<Conta> listar(Solicitante quem, Cpf cpf, int pagina, int tamanho) {
        UUID dono = quem.admin() ? null : quem.usuario();
        return new Pagina<>(contas.listar(cpf, dono, pagina, tamanho), pagina, tamanho, contas.contar(cpf, dono));
    }

    @Transactional
    public Conta depositar(Solicitante quem, UUID id, BigDecimal valor) {
        Conta conta = travar(quem, id);
        movimentos.registrar(conta.depositar(valor, clock.instant()));
        return conta;
    }

    @Transactional
    public Conta sacar(Solicitante quem, UUID id, BigDecimal valor) {
        Conta conta = travar(quem, id);
        movimentos.registrar(conta.sacar(valor, clock.instant()));
        return conta;
    }

    @Transactional(readOnly = true)
    public Extrato extrato(Solicitante quem, UUID id) {
        Conta conta = buscar(quem, id);
        List<Movimento> lancamentos = movimentos.extrato(id);
        return new Extrato(conta, lancamentos);
    }

    /** Depósitos e saques simultâneos na mesma conta passam a ser feitos um de cada vez. */
    private Conta travar(Solicitante quem, UUID id) {
        return contas.buscarParaAtualizar(id).filter(quem::podeAcessar)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    public record Extrato(Conta conta, List<Movimento> movimentos) {
    }
}
