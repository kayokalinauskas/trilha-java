package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.ChaveIdempotenciaReutilizadaException;
import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.ContaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeMovimentos;
import dev.trilhajava.fintech.dominio.RepositorioDeTransferencias;
import dev.trilhajava.fintech.dominio.SaldoInsuficienteException;
import dev.trilhajava.fintech.dominio.Transferencia;
import dev.trilhajava.fintech.dominio.TransferenciaInvalidaException;
import dev.trilhajava.fintech.dominio.TransferenciaNaoEncontradaException;
import dev.trilhajava.fintech.dominio.TransferenciaRealizada;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
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
    private final PublicadorDeEventos eventos;
    private final ObservadorDeTransferencias observador;
    private final Clock clock;

    public ServicoDeTransferencias(RepositorioDeContas contas, RepositorioDeMovimentos movimentos,
            RepositorioDeTransferencias transferencias, PublicadorDeEventos eventos,
            ObservadorDeTransferencias observador, Clock clock) {
        this.contas = contas;
        this.movimentos = movimentos;
        this.transferencias = transferencias;
        this.eventos = eventos;
        this.observador = observador;
        this.clock = clock;
    }

    /**
     * Débito, crédito, registro da transferência e dos dois movimentos: TUDO ou NADA (uma transação).
     * Marco 5: o evento TransferenciaRealizada entra na MESMA transação (outbox) — sem transferência, sem evento.
     */
    @Transactional
    public Resultado transferir(Solicitante quem, String chave, UUID origem, UUID destino, BigDecimal valor) {
        // Marco 6: cada tentativa vira métrica e log. Recusas são contadas aqui; o sucesso, pelo observador,
        // só depois do commit (uma transferência desfeita no commit não pode aparecer como realizada).
        try {
            Resultado resultado = executar(quem, chave, origem, destino, valor);
            if (!resultado.repetida()) {
                observador.realizada(resultado.transferencia());
            }
            return resultado;
        } catch (SaldoInsuficienteException e) {
            observador.recusada(ObservadorDeTransferencias.Motivo.SALDO_INSUFICIENTE);
            throw e;
        } catch (ContaNaoEncontradaException e) {
            observador.recusada(ObservadorDeTransferencias.Motivo.CONTA_NAO_ENCONTRADA);
            throw e;
        } catch (TransferenciaInvalidaException | ChaveIdempotenciaReutilizadaException e) {
            observador.recusada(ObservadorDeTransferencias.Motivo.INVALIDA);
            throw e;
        }
    }

    private Resultado executar(Solicitante quem, String chave, UUID origem, UUID destino, BigDecimal valor) {
        // Marco 4: a origem precisa ser de quem pede. Verificado ANTES da idempotência, para que uma chave
        // conhecida não sirva para consultar a transferência de outra pessoa. Atenção: com "exists", e não
        // carregando a Conta — uma Conta carregada aqui, antes do lock, voltaria VELHA do buscarParaAtualizar
        // (o Hibernate devolve a instância que já está no contexto) e o @Version rejeitaria a gravação.
        if (!quem.admin() && !contas.pertenceA(origem, quem.usuario())) {
            throw new ContaNaoEncontradaException(origem);
        }

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
        eventos.publicar(TransferenciaRealizada.de(transferencia));
        return new Resultado(transferencia, false);
    }

    /** Visível para quem enviou e para quem recebeu (e para o ADMIN). Para os demais, não existe. */
    @Transactional(readOnly = true)
    public Transferencia buscar(Solicitante quem, UUID id) {
        return transferencias.buscar(id)
                .filter(transferencia -> quem.admin() || envolve(quem, transferencia))
                .orElseThrow(() -> new TransferenciaNaoEncontradaException(id));
    }

    private boolean envolve(Solicitante quem, Transferencia transferencia) {
        return Stream.of(transferencia.origem(), transferencia.destino())
                .map(contas::buscar)
                .flatMap(Optional::stream)
                .anyMatch(quem::podeAcessar);
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
