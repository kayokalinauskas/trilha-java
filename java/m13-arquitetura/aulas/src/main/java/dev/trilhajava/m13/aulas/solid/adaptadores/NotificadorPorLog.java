package dev.trilhajava.m13.aulas.solid.adaptadores;

import dev.trilhajava.m13.aulas.solid.Notificador;
import dev.trilhajava.m13.aulas.solid.PedidoFechado;
import org.springframework.stereotype.Component;

/** Adaptador de desenvolvimento: registra no log em vez de mandar e-mail. */
@Component
public class NotificadorPorLog implements Notificador {

    private static final System.Logger LOG = System.getLogger(NotificadorPorLog.class.getName());

    @Override
    public void pedidoFechado(PedidoFechado pedido) {
        LOG.log(System.Logger.Level.INFO, "Pedido {0} fechado: total R$ {1}",
                pedido.numero(), pedido.orcamento().total());
    }
}
