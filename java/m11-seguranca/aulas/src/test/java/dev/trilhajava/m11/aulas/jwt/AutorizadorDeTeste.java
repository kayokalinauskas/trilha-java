package dev.trilhajava.m11.aulas.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Faz o papel do servidor de autorização nos testes: gera pares de chaves RSA,
 * publica as chaves públicas num JWKS (HTTP de verdade, numa porta livre) e assina tokens.
 */
final class AutorizadorDeTeste implements AutoCloseable {

    static final String EMISSOR = "https://auth.banco.test/realms/clientes";
    static final String AUDIENCIA = "extrato-api";

    /** Um autorizador para a JVM inteira: o mesmo jwk-set-uri deixa o Spring reaproveitar o contexto. */
    static final AutorizadorDeTeste COMPARTILHADO = new AutorizadorDeTeste(novaChave("chave-2026-09"));

    private final HttpServer servidor;
    private final AtomicInteger consultasAoJwks = new AtomicInteger();
    private volatile JWKSet publicadas;
    private volatile RSAKey chaveDeAssinatura;

    AutorizadorDeTeste(RSAKey chaveInicial) {
        this.chaveDeAssinatura = chaveInicial;
        this.publicadas = new JWKSet(chaveInicial);
        try {
            servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/jwks", troca -> {
            consultasAoJwks.incrementAndGet();
            // toPublicJWKSet(): só a parte pública sai daqui. Publicar a privada seria entregar a caneta.
            byte[] corpo = publicadas.toPublicJWKSet().toString().getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/jwk-set+json");
            troca.sendResponseHeaders(200, corpo.length);
            try (OutputStream saida = troca.getResponseBody()) {
                saida.write(corpo);
            }
        });
        servidor.start();
    }

    static RSAKey novaChave(String kid) {
        try {
            return new RSAKeyGenerator(2048).keyID(kid).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Não foi possível gerar a chave RSA", e);
        }
    }

    String jwksUri() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/jwks";
    }

    int consultasAoJwks() {
        return consultasAoJwks.get();
    }

    /** Troca o conteúdo do JWKS (é o que o servidor de autorização faz numa rotação). */
    void publicar(RSAKey... chaves) {
        publicadas = new JWKSet(List.<JWK>of(chaves));
    }

    /** Passa a assinar os próximos tokens com outra chave. */
    void assinarCom(RSAKey chave) {
        chaveDeAssinatura = chave;
    }

    RSAKey chaveDeAssinatura() {
        return chaveDeAssinatura;
    }

    /** Claims de um access token "bom": emissor e audiência certos, 5 minutos de validade, jti único. */
    static JwtClaimsSet.Builder claimsValidos() {
        Instant agora = Instant.now();
        return JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .audience(List.of(AUDIENCIA))
                .subject("cliente-8f14e45f")
                .claim("azp", "app-mobile")
                .claim("scope", "extrato:ler")
                .issuedAt(agora)
                .notBefore(agora)
                .expiresAt(agora.plus(Duration.ofMinutes(5)))
                .id(UUID.randomUUID().toString());
    }

    String emitir(JwtClaimsSet claims) {
        return emitir(chaveDeAssinatura, claims);
    }

    /** Assina com a chave indicada, colocando o "kid" dela no cabeçalho. */
    static String emitir(RSAKey chave, JwtClaimsSet claims) {
        return emitir(chave, JwsHeader.with(SignatureAlgorithm.RS256).keyId(chave.getKeyID()).build(), claims);
    }

    static String emitir(RSAKey chave, JwsHeader cabecalho, JwtClaimsSet claims) {
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(chave)));
        return encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
