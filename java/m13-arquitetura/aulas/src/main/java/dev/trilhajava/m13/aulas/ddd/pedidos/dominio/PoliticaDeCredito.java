package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.util.Optional;

/**
 * Serviço de domínio: uma regra de negócio que não pertence a um só objeto.
 * Ela cruza o Pedido com o crédito do cliente e não guarda estado.
 */
public final class PoliticaDeCredito {

    public Optional<String> motivoDeRecusa(Pedido pedido, SituacaoDeCredito credito) {
        if (credito.bloqueado()) {
            return Optional.of("cliente bloqueado no financeiro");
        }
        if (pedido.total().maiorQue(credito.disponivel())) {
            return Optional.of("total de " + pedido.total() + " acima do crédito disponível de " + credito.disponivel());
        }
        return Optional.empty();
    }
}
