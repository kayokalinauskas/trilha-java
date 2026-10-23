package dev.trilhajava.fintech.infra.observabilidade;

import dev.trilhajava.fintech.aplicacao.ObservadorDeTransferencias;
import dev.trilhajava.fintech.dominio.Transferencia;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * fintech.transferencias{resultado=sucesso|saldo_insuficiente|conta_nao_encontrada|invalida} e um log
 * estruturado por transferência realizada. Nada de titular, CPF ou id de usuário em tags: a cardinalidade
 * explodiria, e são dados pessoais.
 */
@Component
class ObservadorDeTransferenciasMicrometer implements ObservadorDeTransferencias {

    private static final Logger log = LoggerFactory.getLogger(ObservadorDeTransferenciasMicrometer.class);

    private final MeterRegistry metricas;

    ObservadorDeTransferenciasMicrometer(MeterRegistry metricas) {
        this.metricas = metricas;
    }

    @Override
    public void realizada(Transferencia transferencia) {
        Runnable registrar = () -> {
            contar("sucesso");
            log.atInfo()
                    .addKeyValue("transferencia", transferencia.id())
                    .addKeyValue("origem", transferencia.origem())
                    .addKeyValue("destino", transferencia.destino())
                    .addKeyValue("valor", transferencia.valor())
                    .log("transferencia realizada");
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    registrar.run();
                }
            });
        } else {
            registrar.run();
        }
    }

    @Override
    public void recusada(Motivo motivo) {
        contar(motivo.name().toLowerCase(Locale.ROOT));
    }

    private void contar(String resultado) {
        Counter.builder("fintech.transferencias")
                .description("Tentativas de transferência, por resultado (repetições idempotentes não contam)")
                .tag("resultado", resultado)
                .register(metricas)
                .increment();
    }
}
