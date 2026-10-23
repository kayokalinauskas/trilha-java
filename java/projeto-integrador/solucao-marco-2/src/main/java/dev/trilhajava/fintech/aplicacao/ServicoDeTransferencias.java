package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.ContaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeMovimentos;
import dev.trilhajava.fintech.dominio.RepositorioDeTransferencias;
import dev.trilhajava.fintech.dominio.Transferencia;
import dev.trilhajava.fintech.dominio.TransferenciaInvalidaException;
import dev.trilhajava.fintech.dominio.TransferenciaNaoEncontradaException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeTransferencias {

    /** O resultado diz se a transferência foi feita agora ou se é a repetição de uma anterior. */
    public record Resultado(Transferencia transferencia, boolean repetida) {
    }

    private final RepositorioDeContas contas;
    private final RepositorioDeMovimentos movimentos;
    private final RepositorioDeTransferencias transferencias;
    private final Clock clock;

    public ServicoDeTransferencias(RepositorioDeContas contas, RepositorioDeMovimentos movimentos,
            RepositorioDeTransferencias transferencias, Clock clock) {
        this.contas = contas;
        this.movimentos = movimentos;
        this.transferencias = transferencias;
        this.clock = clock;
    }

    /**
     * Débito, crédito, registro da transferência e dos dois movimentos: TUDO ou NADA (uma transação).
     */
    @Transactional
    public Resultado transferir(String chave, UUID origem, UUID destino, BigDecimal valor) {
        Optional<Resultado> repeticao = repeticao(chave, origem, destino, valor);
        if (repeticao.isPresent()) {
            return repeticao.get();
        }
        if (origem.equals(destino)) {
            throw new TransferenciaInvalidaException("A conta de origem e a de destino devem ser diferentes");
        }

        // Trava as duas contas SEMPRE na mesma ordem: A→B e B→A simultâneos não entram em deadlock.
        UUID primeira = origem.compareTo(destino) < 0 ? origem : destino;
        UUID segunda = primeira.equals(origem) ? destino : origem;
        Conta contaA = travar(primeira);
        Conta contaB = travar(segunda);

        // Uma requisição idêntica pode ter terminado enquanto esperávamos o lock: confira de novo.
        repeticao = repeticao(chave, origem, destino, valor);
        if (repeticao.isPresent()) {
            return repeticao.get();
        }

        Conta de = contaA.id().equals(origem) ? contaA : contaB;
        Conta para = de == contaA ? contaB : contaA;
        var transferencia = new Transferencia(UUID.randomUUID(), origem, destino, valor, clock.instant(), chave);
        transferencias.salvar(transferencia);
        movimentos.registrar(de.enviar(transferencia));
        movimentos.registrar(para.receber(transferencia));
        return new Resultado(transferencia, false);
    }

    @Transactional(readOnly = true)
    public Transferencia buscar(UUID id) {
        return transferencias.buscar(id).orElseThrow(() -> new TransferenciaNaoEncontradaException(id));
    }

    private Optional<Resultado> repeticao(String chave, UUID origem, UUID destino, BigDecimal valor) {
        return transferencias.buscarPorChave(chave).map(anterior -> {
            anterior.exigirMesmoPedido(origem, destino, valor);
            return new Resultado(anterior, true);
        });
    }

    private Conta travar(UUID id) {
        return contas.buscarParaAtualizar(id).orElseThrow(() -> new ContaNaoEncontradaException(id));
    }
}
