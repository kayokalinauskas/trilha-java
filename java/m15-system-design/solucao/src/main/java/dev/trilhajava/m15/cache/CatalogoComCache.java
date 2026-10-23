package dev.trilhajava.m15.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cache-aside: a aplicação consulta o Redis; na falta, busca no banco e grava no Redis. Mais quatro
 * cuidados que separam um cache de produção de um de tutorial:
 * <ol>
 *   <li>TTL com jitter: chaves gravadas juntas não expiram juntas (sem avalanche no banco).</li>
 *   <li>Cache negativo: "não existe" também é cacheado, por pouco tempo (sem martelar o banco com ids falsos).</li>
 *   <li>Single-flight: N requisições simultâneas pela mesma chave fria fazem UMA consulta (sem stampede).</li>
 *   <li>Redis fora do ar é lentidão, não erro: a leitura cai direto no banco.</li>
 * </ol>
 */
@Service
public class CatalogoComCache {

    public static final Duration TTL = Duration.ofMinutes(10);
    public static final Duration JITTER_MAXIMO = Duration.ofSeconds(60);
    public static final Duration TTL_NEGATIVO = Duration.ofSeconds(30);
    /** O que vai no Redis quando o produto não existe. */
    public static final String AUSENTE = "__ausente__";

    private static final Logger log = LoggerFactory.getLogger(CatalogoComCache.class);

    private final BancoDeProdutos banco;
    private final StringRedisTemplate redis;
    private final JsonMapper json;
    private final Map<String, CompletableFuture<Optional<Produto>>> emVoo = new ConcurrentHashMap<>();

    public CatalogoComCache(BancoDeProdutos banco, StringRedisTemplate redis, JsonMapper json) {
        this.banco = banco;
        this.redis = redis;
        this.json = json;
    }

    public static String chave(String id) {
        return "produto:" + id;
    }

    public Optional<Produto> buscar(String id) {
        String chave = chave(id);
        String emCache;
        try {
            emCache = redis.opsForValue().get(chave);
        } catch (DataAccessException e) {
            // Sem cache, o sistema fica mais lento, mas continua respondendo.
            log.warn("Redis indisponível, lendo do banco: {}", e.getMessage());
            return banco.buscar(id);
        }
        if (emCache != null) {
            return AUSENTE.equals(emCache) ? Optional.empty() : Optional.of(json.readValue(emCache, Produto.class));
        }
        return carregarUmaVez(id, chave);
    }

    /** A primeira thread carrega; as outras que chegarem enquanto isso esperam o mesmo resultado. */
    private Optional<Produto> carregarUmaVez(String id, String chave) {
        CompletableFuture<Optional<Produto>> novo = new CompletableFuture<>();
        CompletableFuture<Optional<Produto>> existente = emVoo.putIfAbsent(chave, novo);
        if (existente != null) {
            return existente.join();
        }
        try {
            Optional<Produto> produto = banco.buscar(id);
            gravar(chave, produto);
            novo.complete(produto);
            return produto;
        } catch (RuntimeException e) {
            novo.completeExceptionally(e);
            throw e;
        } finally {
            emVoo.remove(chave, novo);
        }
    }

    private void gravar(String chave, Optional<Produto> produto) {
        try {
            if (produto.isPresent()) {
                redis.opsForValue().set(chave, json.writeValueAsString(produto.get()), ttlComJitter());
            } else {
                redis.opsForValue().set(chave, AUSENTE, TTL_NEGATIVO);
            }
        } catch (DataAccessException e) {
            log.warn("Não foi possível gravar no Redis: {}", e.getMessage());
        }
    }

    private static Duration ttlComJitter() {
        return TTL.plusMillis(ThreadLocalRandom.current().nextLong(JITTER_MAXIMO.toMillis() + 1));
    }

    /**
     * Grava no banco e APAGA a chave (não atualiza o cache). Atualizar o cache aqui abre uma corrida:
     * duas atualizações simultâneas podem gravar no Redis na ordem inversa da do banco, e o valor velho
     * fica no cache até o TTL. Apagando, a próxima leitura busca o valor atual.
     */
    public void atualizar(Produto produto) {
        banco.salvar(produto);
        try {
            redis.delete(chave(produto.id()));
        } catch (DataAccessException e) {
            log.warn("Não foi possível invalidar {} no Redis: {}", produto.id(), e.getMessage());
        }
    }
}
