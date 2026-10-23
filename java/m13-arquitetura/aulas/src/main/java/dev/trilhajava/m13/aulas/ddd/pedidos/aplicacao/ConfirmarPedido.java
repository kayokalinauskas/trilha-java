package dev.trilhajava.m13.aulas.ddd.pedidos.aplicacao;

import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Pedido;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.PedidoId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.PoliticaDeCredito;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.RegraDoPedidoException;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.RepositorioDePedidos;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.SituacaoDeCredito;
import java.time.Clock;
import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * Serviço de aplicação (caso de uso): orquestra, não decide. Carrega UM agregado,
 * pede ao domínio que aplique as regras, salva e publica os eventos.
 * Em produção, este método seria a fronteira da transação (@Transactional).
 */
public final class ConfirmarPedido {

    private final RepositorioDePedidos pedidos;
    private final ConsultaDeCredito credito;
    private final PublicadorDeEventos publicador;
    private final Clock relogio;
    private final PoliticaDeCredito politica = new PoliticaDeCredito();

    public ConfirmarPedido(RepositorioDePedidos pedidos, ConsultaDeCredito credito,
                           PublicadorDeEventos publicador, Clock relogio) {
        this.pedidos = pedidos;
        this.credito = credito;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public void executar(PedidoId id) {
        Pedido pedido = pedidos.porId(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado: " + id));
        SituacaoDeCredito situacao = credito.situacaoDe(pedido.cliente());
        politica.motivoDeRecusa(pedido, situacao).ifPresent(motivo -> {
            throw new RegraDoPedidoException("Pedido " + id + " recusado: " + motivo);
        });
        pedido.confirmar(Instant.now(relogio));
        pedidos.salvar(pedido);
        publicador.publicar(pedido.retirarEventos());
    }
}
