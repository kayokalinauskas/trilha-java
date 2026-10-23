package dev.trilhajava.m16.ferramentas;

import java.math.BigDecimal;

/** Um Pix que o assistente preparou, mas que só sai da conta quando o cliente confirma no app. */
public record PropostaDePix(String id, String cliente, String chave, BigDecimal valor, Status status) {

    public enum Status {
        PENDENTE,
        CONFIRMADA
    }
}
