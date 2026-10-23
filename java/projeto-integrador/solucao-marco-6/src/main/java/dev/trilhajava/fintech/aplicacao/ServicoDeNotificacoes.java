package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Notificacao;
import dev.trilhajava.fintech.dominio.RepositorioDeContas;
import dev.trilhajava.fintech.dominio.RepositorioDeNotificacoes;
import dev.trilhajava.fintech.dominio.TipoNotificacao;
import dev.trilhajava.fintech.dominio.TransferenciaRealizada;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Marco 5: transforma eventos de transferência em avisos para os donos das contas. */
@Service
public class ServicoDeNotificacoes {

    static final String CONSUMIDOR = "notificacoes";
    static final int LIMITE = 50;

    private final RepositorioDeNotificacoes notificacoes;
    private final RepositorioDeContas contas;
    private final RegistroDeEventosProcessados processados;
    private final Clock clock;

    public ServicoDeNotificacoes(RepositorioDeNotificacoes notificacoes, RepositorioDeContas contas,
            RegistroDeEventosProcessados processados, Clock clock) {
        this.notificacoes = notificacoes;
        this.contas = contas;
        this.processados = processados;
        this.clock = clock;
    }

    /**
     * Idempotente: o evento pode chegar mais de uma vez (o relay e o Kafka garantem "pelo menos uma").
     * A marca de processado e as notificações são gravadas na mesma transação.
     *
     * @return false se o evento já tinha sido processado
     */
    @Transactional
    public boolean registrar(UUID evento, TransferenciaRealizada transferencia) {
        if (!processados.registrar(CONSUMIDOR, evento)) {
            return false;
        }
        avisar(transferencia.origem(), TipoNotificacao.TRANSFERENCIA_ENVIADA, transferencia);
        avisar(transferencia.destino(), TipoNotificacao.TRANSFERENCIA_RECEBIDA, transferencia);
        return true;
    }

    private void avisar(UUID idConta, TipoNotificacao tipo, TransferenciaRealizada transferencia) {
        contas.buscar(idConta)
                .map(Conta::dono)
                .ifPresent(dono -> notificacoes.salvar(new Notificacao(UUID.randomUUID(), dono, tipo,
                        transferencia.transferencia(), idConta, transferencia.valor(), clock.instant())));
    }

    @Transactional(readOnly = true)
    public List<Notificacao> recentes(Solicitante quem) {
        return notificacoes.recentes(quem.usuario(), LIMITE);
    }
}
