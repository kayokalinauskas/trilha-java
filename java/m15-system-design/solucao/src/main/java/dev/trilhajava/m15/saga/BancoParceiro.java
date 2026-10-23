package dev.trilhajava.m15.saga;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * O banco da conta de destino, acessado pela rede. Ele deduplica pelo id da transferência: receber o mesmo
 * id duas vezes não credita duas vezes.
 */
public interface BancoParceiro {

    enum Resposta { ACEITA, RECUSADA }

    /**
     * @throws ParceiroIndisponivelException timeout ou falha de rede: a transferência PODE ter chegado lá
     */
    Resposta enviar(String idTransferencia, String contaDestino, BigDecimal valor);

    /**
     * O que o parceiro sabe sobre a transferência. Vazio: ele nunca a recebeu.
     *
     * @throws ParceiroIndisponivelException o parceiro continua sem responder
     */
    Optional<Resposta> consultar(String idTransferencia);
}
