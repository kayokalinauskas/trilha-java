package dev.trilhajava.m12.aulas.retry;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Ouve o DefaultErrorHandler: cada tentativa que falhou e cada mensagem que foi para o DLT.
 * Em produção, os contadores viram métricas (Micrometer, módulo 14) e o "enviadas ao DLT" vira alerta.
 */
@Component
public class MonitorDeFalhas implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(MonitorDeFalhas.class);

    private final AtomicInteger tentativasQueFalharam = new AtomicInteger();
    private final AtomicInteger enviadasAoDlt = new AtomicInteger();

    @Override
    public void failedDelivery(ConsumerRecord<?, ?> registro, Exception erro, int tentativa) {
        tentativasQueFalharam.incrementAndGet();
        log.info("tentativa {} falhou: {}-{}@{} chave={} causa={}", tentativa, registro.topic(),
                registro.partition(), registro.offset(), registro.key(), causa(erro));
    }

    @Override
    public void recovered(ConsumerRecord<?, ?> registro, Exception erro) {
        enviadasAoDlt.incrementAndGet();
        log.warn("desistindo: {}-{}@{} chave={} foi para o DLT. causa={}", registro.topic(),
                registro.partition(), registro.offset(), registro.key(), causa(erro));
    }

    @Override
    public void recoveryFailed(ConsumerRecord<?, ?> registro, Exception original, Exception falha) {
        log.error("NÃO foi possível publicar {}-{}@{} no DLT; a mensagem será tentada de novo", registro.topic(),
                registro.partition(), registro.offset(), falha);
    }

    public int tentativasQueFalharam() {
        return tentativasQueFalharam.get();
    }

    public int enviadasAoDlt() {
        return enviadasAoDlt.get();
    }

    private static String causa(Exception erro) {
        Throwable raiz = NestedExceptionUtils.getMostSpecificCause(erro);
        return raiz.getClass().getSimpleName() + ": " + raiz.getMessage();
    }
}
