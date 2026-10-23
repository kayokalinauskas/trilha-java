package dev.trilhajava.m13.loja.faturamento.internal;

import dev.trilhajava.m13.loja.faturamento.AutorizadorFiscal;
import dev.trilhajava.m13.loja.faturamento.Fatura;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Emite a fatura de um pedido. Hoje é chamado diretamente pelo módulo pedidos. */
@Component
public class EmissaoDeFaturas {

    private final AutorizadorFiscal autorizador;
    private final RegistroDeFaturas registro;

    EmissaoDeFaturas(AutorizadorFiscal autorizador, RegistroDeFaturas registro) {
        this.autorizador = autorizador;
        this.registro = registro;
    }

    public void emitir(UUID pedido, String cliente, BigDecimal total) {
        String chave = autorizador.autorizar(pedido, total);
        registro.salvar(new Fatura(pedido, cliente, total, chave));
    }
}
