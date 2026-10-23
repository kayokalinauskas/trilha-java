package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;

/** Avisa o cliente (e-mail, push, SMS: não importa para o serviço). */
public interface Notificador {

    void reembolsoConcluido(String cliente, String numeroPedido, BigDecimal valor);
}
