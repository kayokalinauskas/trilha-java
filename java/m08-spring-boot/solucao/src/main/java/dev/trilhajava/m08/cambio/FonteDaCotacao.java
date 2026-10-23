package dev.trilhajava.m08.cambio;

public enum FonteDaCotacao {
    /** Buscada agora na API. */
    API,
    /** Reaproveitada do cache, ainda dentro da validade. */
    CACHE,
    /** A API falhou; usamos a última cotação conhecida (vencida, mas dentro do limite de fallback). */
    CACHE_EXPIRADO
}
