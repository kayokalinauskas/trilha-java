package dev.trilhajava.m15.cache;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cache-aside com Redis na frente do BancoDeProdutos. Os requisitos (TTL com jitter, cache negativo,
 * single-flight, Redis fora do ar) estão no enunciado.
 */
@Service
public class CatalogoComCache {

    public static final Duration TTL = Duration.ofMinutes(10);
    public static final Duration JITTER_MAXIMO = Duration.ofSeconds(60);
    public static final Duration TTL_NEGATIVO = Duration.ofSeconds(30);
    /** O que vai no Redis quando o produto não existe. */
    public static final String AUSENTE = "__ausente__";

    private final BancoDeProdutos banco;
    private final StringRedisTemplate redis;
    private final JsonMapper json;

    public CatalogoComCache(BancoDeProdutos banco, StringRedisTemplate redis, JsonMapper json) {
        this.banco = banco;
        this.redis = redis;
        this.json = json;
    }

    public static String chave(String id) {
        return "produto:" + id;
    }

    public Optional<Produto> buscar(String id) {
        // TODO: exercício 15.01
        throw new UnsupportedOperationException("TODO: exercício 15.01");
    }

    public void atualizar(Produto produto) {
        // TODO: grave no banco e invalide o cache
        throw new UnsupportedOperationException("TODO: exercício 15.01");
    }
}
