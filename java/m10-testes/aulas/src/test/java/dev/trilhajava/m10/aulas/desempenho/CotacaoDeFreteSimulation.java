package dev.trilhajava.m10.aulas.desempenho;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.listFeeder;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Teste de carga da cotação de frete, em modelo ABERTO: chegam N novas requisições por segundo,
 * não importa se as anteriores já terminaram (como clientes de verdade num checkout).
 *
 * <p>Parâmetros (todos opcionais), passados como -D na linha de comando:
 * versao (v1 ou v2), taxa (requisições por segundo), segundos (duração do patamar) e url.
 */
public class CotacaoDeFreteSimulation extends Simulation {

    static final String VERSAO = System.getProperty("versao", "v1");
    static final int TAXA = Integer.getInteger("taxa", 100);
    static final int SEGUNDOS = Integer.getInteger("segundos", 30);
    static final String URL = System.getProperty("url", "http://localhost:8088");

    // Um CEP de cada região, em rodízio, para não medir sempre o mesmo caminho.
    static final FeederBuilder<Object> CEPS = listFeeder(List.<Map<String, Object>>of(
            Map.of("cep", "01310100"),
            Map.of("cep", "13015000"),
            Map.of("cep", "30130000"),
            Map.of("cep", "90010000"))).circular();

    // Quem chama a cotação é o serviço de checkout, que reaproveita conexões HTTP (keep-alive).
    // Sem shareConnections(), cada usuário virtual abre uma conexão TCP nova, como um navegador.
    static final HttpProtocolBuilder PROTOCOLO = http.baseUrl(URL)
            .acceptHeader("application/json")
            .shareConnections();

    static final ScenarioBuilder COTAR = scenario("Cotação de frete " + VERSAO)
            .feed(CEPS)
            .exec(http("GET /" + VERSAO + "/fretes/cotacao")
                    .get("/" + VERSAO + "/fretes/cotacao")
                    .queryParam("cep", "#{cep}")
                    .check(status().is(200)));

    {
        setUp(COTAR.injectOpen(
                        // 10 s de rampa: a JVM do servidor aquece antes do patamar.
                        rampUsersPerSec(1).to(TAXA).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(TAXA).during(Duration.ofSeconds(SEGUNDOS))))
                .protocols(PROTOCOLO)
                .assertions(
                        global().responseTime().percentile(95.0).lt(200),
                        global().failedRequests().percent().lt(1.0));
    }
}
