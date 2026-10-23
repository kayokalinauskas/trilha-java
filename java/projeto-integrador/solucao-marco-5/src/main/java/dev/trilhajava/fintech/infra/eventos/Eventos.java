package dev.trilhajava.fintech.infra.eventos;

/** Nomes do contrato de mensageria. */
final class Eventos {

    static final String TOPICO = "fintech.transferencias";
    static final String TOPICO_DLT = TOPICO + "-dlt";
    static final String GRUPO_NOTIFICACOES = "fintech-notificacoes";

    private Eventos() {
    }
}
