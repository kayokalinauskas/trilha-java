package dev.trilhajava.m11.aulas.senhas;

import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consulta senhas vazadas na API Pwned Passwords com k-anonimato: só os 5 primeiros caracteres
 * do SHA-1 da senha saem do servidor. A API devolve todos os sufixos que começam com esse
 * prefixo (centenas), e a comparação final acontece aqui dentro.
 */
public class PwnedPasswordsPorFaixa implements VerificadorDeSenhasVazadas {

    /** Recebe o prefixo de 5 caracteres e devolve o corpo da resposta ("SUFIXO:CONTAGEM" por linha). */
    @FunctionalInterface
    public interface ConsultaDeFaixa {
        String buscar(String prefixo);
    }

    private static final System.Logger LOG = System.getLogger(PwnedPasswordsPorFaixa.class.getName());

    private final ConsultaDeFaixa consulta;

    public PwnedPasswordsPorFaixa(ConsultaDeFaixa consulta) {
        this.consulta = consulta;
    }

    /** A consulta real: GET https://api.pwnedpasswords.com/range/{prefixo}, com preenchimento. */
    public static PwnedPasswordsPorFaixa viaHttp(RestClient.Builder builder) {
        RestClient cliente = builder.baseUrl("https://api.pwnedpasswords.com").build();
        return new PwnedPasswordsPorFaixa(prefixo -> cliente.get()
                .uri("/range/{prefixo}", prefixo)
                .header("Add-Padding", "true")
                .retrieve()
                .body(String.class));
    }

    @Override
    public boolean vazada(String senha) {
        String sha1 = sha1Maiusculo(senha);
        String prefixo = sha1.substring(0, 5);
        String sufixo = sha1.substring(5);
        String corpo;
        try {
            corpo = consulta.buscar(prefixo);
        } catch (RestClientException e) {
            // Decisão consciente: com a API fora do ar, o cadastro segue (fail-open) e fica no log.
            LOG.log(Level.WARNING, "Pwned Passwords indisponível; senha aceita sem a checagem", e);
            return false;
        }
        if (corpo == null) {
            return false;
        }
        for (String linha : corpo.split("\r?\n")) {
            int doisPontos = linha.indexOf(':');
            if (doisPontos < 0) {
                continue;
            }
            boolean mesmoSufixo = linha.substring(0, doisPontos).equalsIgnoreCase(sufixo);
            // Linhas de preenchimento (Add-Padding) vêm com contagem 0 e não contam.
            long vezes = Long.parseLong(linha.substring(doisPontos + 1).strip());
            if (mesmoSufixo && vezes > 0) {
                return true;
            }
        }
        return false;
    }

    static String sha1Maiusculo(String senha) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(senha.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Toda JVM tem SHA-1", e);
        }
    }
}
