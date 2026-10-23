package dev.trilhajava.m12.aulas.fundamentos;

/** Em que ordem o consumidor faz as duas coisas: confirmar o offset e processar. */
public enum Garantia {
    /** Confirma ANTES de processar: se cair no meio, a mensagem se perde. */
    NO_MAXIMO_UMA_VEZ,
    /** Processa ANTES de confirmar: se cair no meio, a mensagem volta (duplicata). */
    PELO_MENOS_UMA_VEZ
}
