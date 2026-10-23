package dev.trilhajava.m08.cambio;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * cambio.base-url=https://api.cotacoes.dev
 * cambio.timeout=2s
 * cambio.validade=60s
 * cambio.validade-maxima-fallback=24h
 *
 * Duration aceita "500ms", "2s", "24h"... O Spring converte sozinho.
 */
@ConfigurationProperties("cambio")
public record CambioProperties(
        URI baseUrl,
        @DefaultValue("2s") Duration timeout,
        @DefaultValue("60s") Duration validade,
        @DefaultValue("24h") Duration validadeMaximaFallback) {
}
