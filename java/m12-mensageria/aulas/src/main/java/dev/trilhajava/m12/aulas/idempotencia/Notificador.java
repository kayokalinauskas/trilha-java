package dev.trilhajava.m12.aulas.idempotencia;

import java.math.BigDecimal;

/** Um efeito FORA do banco (push, e-mail): um rollback não consegue desfazê-lo. */
public interface Notificador {

    void cashbackCreditado(String contaId, BigDecimal valor);
}
