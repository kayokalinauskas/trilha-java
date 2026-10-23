package dev.trilhajava.m13.loja.faturamento;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A integração com a SEFAZ, que autoriza a nota fiscal e devolve a chave de acesso.
 * Pode ficar fora do ar — e fica, em dia de pico.
 */
public interface AutorizadorFiscal {

    String autorizar(UUID pedido, BigDecimal total);
}
