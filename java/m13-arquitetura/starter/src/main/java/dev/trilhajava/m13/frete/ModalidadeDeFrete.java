package dev.trilhajava.m13.frete;

import java.util.Optional;

/** Strategy: uma forma de entregar. Vazio = esta modalidade não atende o pedido. */
public interface ModalidadeDeFrete {

    Optional<Cotacao> cotar(Pedido pedido);
}
