package dev.trilhajava.m13.aulas.resiliencia;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * Faz UMA chamada HTTP à operadora e traduz o resultado em tipos do domínio.
 * Não repete nada, não guarda estado: as defesas ficam nas camadas de cima.
 * O que esta classe garante é a primeira defesa de todas: nenhuma chamada espera para sempre.
 */
public class ClienteDaOperadora {

    private final RestClient http;

    public ClienteDaOperadora(String urlBase, Duration timeoutDeConexao, Duration timeoutDeLeitura) {
        HttpClient jdk = HttpClient.newBuilder()
                .connectTimeout(timeoutDeConexao)          // abrir o socket
                .build();
        var fabrica = new JdkClientHttpRequestFactory(jdk);
        fabrica.setReadTimeout(timeoutDeLeitura);          // esperar a resposta, depois de conectado
        this.http = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .build();
    }

    /** GET: idempotente, pode ser repetido à vontade. */
    public RespostaDaOperadora consultarElegibilidade(String carteirinha) {
        try {
            return http.get()
                    .uri("/elegibilidade/{carteirinha}", carteirinha)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (requisicao, resposta) -> {
                        throw new CarteirinhaDesconhecidaException(carteirinha);
                    })
                    .onStatus(status -> status.is5xxServerError(), (requisicao, resposta) -> {
                        throw new OperadoraIndisponivelException("operadora respondeu " + resposta.getStatusCode(), null);
                    })
                    .body(RespostaDaOperadora.class);
        } catch (RestClientException e) {
            throw traduzir(e);
        }
    }

    /**
     * Timeout de conexão ou de leitura, conexão recusada ou resetada: tudo transitório.
     * Cuidado: se o tempo estoura no meio do CORPO da resposta, o RestClient não lança
     * ResourceAccessException, e sim "Error while extracting response...", com um IOException na causa.
     */
    private static RuntimeException traduzir(RestClientException e) {
        if (e instanceof ResourceAccessException || e.getMostSpecificCause() instanceof IOException) {
            return new OperadoraIndisponivelException("operadora não respondeu a tempo: " + e.getMessage(), e);
        }
        return e;
    }

    /**
     * POST: NÃO é idempotente por natureza. Só pode ser repetido porque leva a chave de idempotência,
     * gerada UMA vez por quem chama e reaproveitada em todas as tentativas.
     */
    public String solicitarAutorizacao(String carteirinha, String procedimento, String chaveDeIdempotencia) {
        try {
            Map<?, ?> resposta = http.post()
                    .uri("/autorizacoes")
                    .header("Idempotency-Key", chaveDeIdempotencia)
                    .body(Map.of("carteirinha", carteirinha, "procedimento", procedimento))
                    .retrieve()
                    .onStatus(status -> status.is5xxServerError(), (requisicao, res) -> {
                        throw new OperadoraIndisponivelException("operadora respondeu " + res.getStatusCode(), null);
                    })
                    .body(Map.class);
            return String.valueOf(resposta.get("numero"));
        } catch (RestClientException e) {
            throw traduzir(e);
        }
    }
}
