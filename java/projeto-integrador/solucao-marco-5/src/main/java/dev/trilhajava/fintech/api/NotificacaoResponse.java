package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.Notificacao;
import dev.trilhajava.fintech.dominio.TipoNotificacao;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record NotificacaoResponse(UUID id, TipoNotificacao tipo, UUID transferencia, UUID conta, BigDecimal valor,
        String mensagem, Instant criadaEm) {

    static NotificacaoResponse de(Notificacao n) {
        return new NotificacaoResponse(n.id(), n.tipo(), n.transferencia(), n.conta(), n.valor(), n.mensagem(),
                n.criadaEm());
    }
}
