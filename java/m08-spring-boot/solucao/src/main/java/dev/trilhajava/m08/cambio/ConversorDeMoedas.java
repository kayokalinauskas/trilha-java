package dev.trilhajava.m08.cambio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
public class ConversorDeMoedas {

    private static final Logger log = LoggerFactory.getLogger(ConversorDeMoedas.class);

    private record CotacaoEmCache(Cotacao cotacao, Instant obtidaEm) {
        Duration idade(Instant agora) {
            return Duration.between(obtidaEm, agora);
        }
    }

    private final CotacaoClient client;
    private final CambioProperties properties;
    private final Clock clock;
    private final Map<String, CotacaoEmCache> cache = new ConcurrentHashMap<>();

    public ConversorDeMoedas(CotacaoClient client, CambioProperties properties, Clock clock) {
        this.client = client;
        this.properties = properties;
        this.clock = clock;
    }

    public Conversao converter(BigDecimal valorEmReais, String moeda) {
        if (valorEmReais == null || valorEmReais.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }
        if (moeda == null || moeda.isBlank()) {
            throw new IllegalArgumentException("Informe a moeda");
        }
        String codigo = moeda.strip().toUpperCase(Locale.ROOT);
        Instant agora = clock.instant();

        CotacaoEmCache emCache = cache.get(codigo);
        if (emCache != null && emCache.idade(agora).compareTo(properties.validade()) <= 0) {
            return converter(valorEmReais, emCache.cotacao(), FonteDaCotacao.CACHE);
        }

        try {
            Cotacao nova = client.buscar(codigo);
            cache.put(codigo, new CotacaoEmCache(nova, agora));
            return converter(valorEmReais, nova, FonteDaCotacao.API);
        } catch (HttpClientErrorException.NotFound e) {
            throw new MoedaNaoSuportadaException(codigo);
        } catch (RestClientException e) {
            // 5xx, timeout, conexão recusada: degradamos com elegância se houver algo recente o bastante.
            if (emCache != null && emCache.idade(agora).compareTo(properties.validadeMaximaFallback()) <= 0) {
                log.warn("API de cotações indisponível; usando cotação de {} para {}", emCache.obtidaEm(), codigo);
                return converter(valorEmReais, emCache.cotacao(), FonteDaCotacao.CACHE_EXPIRADO);
            }
            throw new CotacaoIndisponivelException(codigo, e);
        }
    }

    private static Conversao converter(BigDecimal valorEmReais, Cotacao cotacao, FonteDaCotacao fonte) {
        BigDecimal convertido = valorEmReais.divide(cotacao.venda(), 2, RoundingMode.HALF_EVEN);
        return new Conversao(cotacao.moeda(), valorEmReais, convertido, cotacao.venda(), fonte);
    }
}
