package dev.trilhajava.m15.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = CacheApplication.class, properties = "spring.data.redis.timeout=300ms")
@Testcontainers
@EnabledIfDockerAvailable
@DisplayName("Exercício 15.01 — cache-aside com Redis")
class CacheAsideTest {

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    @Autowired
    CatalogoComCache catalogo;

    @Autowired
    BancoDeProdutos banco;

    @Autowired
    StringRedisTemplate redis;

    @BeforeEach
    void limpar() {
        redis.execute(conexao -> {
            conexao.serverCommands().flushAll();
            return null;
        }, true);
        banco.zerarContagem();
    }

    long ttlEmSegundos(String id) {
        return redis.getExpire(CatalogoComCache.chave(id), TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("a primeira leitura vai ao banco; a segunda sai do Redis")
    void segundaLeituraDoCache() {
        Optional<Produto> primeira = catalogo.buscar("p1");
        Optional<Produto> segunda = catalogo.buscar("p1");

        assertThat(primeira).contains(new Produto("p1", "Produto 1", new BigDecimal("1.90")));
        assertThat(segunda).isEqualTo(primeira);
        assertThat(banco.consultas()).isEqualTo(1);
    }

    @Test
    @DisplayName("no Redis, a chave é produto:{id} e o valor é o produto em JSON")
    void formatoNoRedis() {
        catalogo.buscar("p2");

        String valor = redis.opsForValue().get("produto:p2");
        assertThat(valor).isNotNull();
        assertThat(JsonPath.<String>read(valor, "$.nome")).isEqualTo("Produto 2");
        assertThat(new BigDecimal(JsonPath.read(valor, "$.preco").toString())).isEqualByComparingTo("2.90");
    }

    @Test
    @DisplayName("TTL de 10 minutos mais um jitter de até 60 segundos")
    void ttl() {
        catalogo.buscar("p3");

        assertThat(ttlEmSegundos("p3")).isBetween(595L, 660L);
    }

    @Test
    @DisplayName("jitter: produtos carregados juntos não expiram todos no mesmo segundo")
    void jitter() {
        Set<Long> ttls = new HashSet<>();
        for (int i = 10; i < 40; i++) {
            catalogo.buscar("p" + i);
            ttls.add(ttlEmSegundos("p" + i));
        }

        assertThat(ttls).as("TTLs distintos em 30 chaves").hasSizeGreaterThanOrEqualTo(8);
    }

    @Test
    @DisplayName("cache negativo: um id inexistente vai ao banco uma vez e fica marcado por até 30 s")
    void cacheNegativo() {
        assertThat(catalogo.buscar("nao-existe")).isEmpty();
        assertThat(catalogo.buscar("nao-existe")).isEmpty();

        assertThat(banco.consultas()).isEqualTo(1);
        assertThat(ttlEmSegundos("nao-existe")).isBetween(1L, 30L);
    }

    @Test
    @DisplayName("atualizar grava no banco e invalida a chave: a próxima leitura traz o preço novo")
    void invalidacao() {
        catalogo.buscar("p4");

        catalogo.atualizar(new Produto("p4", "Produto 4", new BigDecimal("99.00")));

        assertThat(redis.hasKey("produto:p4")).as("a chave foi apagada").isFalse();
        assertThat(catalogo.buscar("p4")).map(Produto::preco).contains(new BigDecimal("99.00"));
    }

    @Test
    @DisplayName("single-flight: 20 leituras simultâneas da mesma chave fria fazem UMA consulta ao banco")
    void singleFlight() throws Exception {
        int threads = 20;
        CountDownLatch largada = new CountDownLatch(1);
        List<Callable<Optional<Produto>>> tarefas = IntStream.range(0, threads)
                .<Callable<Optional<Produto>>>mapToObj(i -> () -> {
                    largada.await();
                    return catalogo.buscar("p5");
                }).toList();

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<Optional<Produto>>> resultados = tarefas.stream().map(executor::submit).toList();
            largada.countDown();
            for (Future<Optional<Produto>> r : resultados) {
                assertThat(r.get(5, TimeUnit.SECONDS)).map(Produto::id).contains("p5");
            }
        }

        assertThat(banco.consultas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redis fora do ar: a leitura cai no banco, sem exceção e sem esperar minutos")
    void redisForaDoAr() {
        catalogo.buscar("p6");
        var docker = REDIS.getDockerClient();
        docker.pauseContainerCmd(REDIS.getContainerId()).exec();
        try {
            long inicio = System.nanoTime();

            Optional<Produto> produto = catalogo.buscar("p7");

            assertThat(produto).map(Produto::nome).contains("Produto 7");
            assertThat(Duration.ofNanos(System.nanoTime() - inicio)).isLessThan(Duration.ofSeconds(3));
        } finally {
            docker.unpauseContainerCmd(REDIS.getContainerId()).exec();
        }
        assertThat(catalogo.buscar("p6")).as("com o Redis de volta, o cache volta a funcionar").isPresent();
    }
}
