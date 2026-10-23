package dev.trilhajava.m13.loja.faturamento.internal;

import dev.trilhajava.m13.loja.faturamento.AutorizadorFiscal;
import dev.trilhajava.m13.loja.faturamento.Fatura;
import dev.trilhajava.m13.loja.pedidos.PedidoConfirmado;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Emite a fatura quando um pedido é confirmado. Se a SEFAZ falhar, a exceção sobe, a
 * publicação do evento fica INCOMPLETA no registro, e pode ser reenviada depois — o pedido
 * continua confirmado, e nada se perde.
 */
@Component
class EmissaoDeFaturas {

    private final AutorizadorFiscal autorizador;
    private final RegistroDeFaturas registro;

    EmissaoDeFaturas(AutorizadorFiscal autorizador, RegistroDeFaturas registro) {
        this.autorizador = autorizador;
        this.registro = registro;
    }

    @ApplicationModuleListener
    void quandoConfirmado(PedidoConfirmado evento) {
        if (registro.doPedido(evento.pedido()).isPresent()) {
            return;   // reentrega: a fatura já existe
        }
        String chave = autorizador.autorizar(evento.pedido(), evento.total());
        registro.salvar(new Fatura(evento.pedido(), evento.cliente(), evento.total(), chave));
    }
}
