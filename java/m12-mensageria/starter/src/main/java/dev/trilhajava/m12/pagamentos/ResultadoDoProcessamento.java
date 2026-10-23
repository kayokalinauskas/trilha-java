package dev.trilhajava.m12.pagamentos;

public enum ResultadoDoProcessamento {
    /** O efeito foi aplicado agora. */
    PROCESSADO,
    /** Esta mensagem já tinha sido processada: nada mudou. */
    DUPLICADO,
    /** A mensagem é nova, mas mais antiga que o estado atual do pedido: registrada e ignorada. */
    OBSOLETO
}
