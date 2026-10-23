package dev.trilhajava.m11.aulas.oidc;

import dev.trilhajava.m11.aulas.oidc.autorizacao.ServidorDeAutorizacaoApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.test.util.TestSocketUtils;

/**
 * Um servidor de autorização de verdade (HTTP numa porta real) por JVM de testes. Sobe na
 * primeira vez que alguém usa a classe e para quando a JVM termina. Os segredos abaixo
 * existem só aqui; "{noop}" diz ao PasswordEncoder que o valor está em texto puro.
 */
public final class ServidorDeAutorizacaoDeTeste {

    public static final String SEGREDO_FATURAMENTO = "segredo-de-teste-faturamento";
    public static final String SEGREDO_RELATORIOS = "segredo-de-teste-relatorios";
    public static final String SEGREDO_AUDITORIA = "segredo-de-teste-auditoria";
    public static final String SEGREDO_PORTAL = "segredo-de-teste-portal";
    public static final String SENHA_DA_ANA = "senha-de-teste-ana";

    private static final int PORTA = TestSocketUtils.findAvailableTcpPort();

    static {
        new SpringApplicationBuilder(ServidorDeAutorizacaoApplication.class)
                .properties("spring.config.name=servidor-de-autorizacao")
                .run("--server.port=" + PORTA,
                        "--EMISSOR=" + emissor(),
                        "--FATURAMENTO_CLIENT_SECRET={noop}" + SEGREDO_FATURAMENTO,
                        "--RELATORIOS_CLIENT_SECRET={noop}" + SEGREDO_RELATORIOS,
                        "--AUDITORIA_CLIENT_SECRET={noop}" + SEGREDO_AUDITORIA,
                        "--PORTAL_CLIENT_SECRET={noop}" + SEGREDO_PORTAL,
                        "--SENHA_DA_ANA={noop}" + SENHA_DA_ANA);
    }

    private ServidorDeAutorizacaoDeTeste() {
    }

    /** Ex.: http://localhost:52811 — é o valor do claim iss em todos os tokens. */
    public static String emissor() {
        return "http://localhost:" + PORTA;
    }

    public static String endpointDeToken() {
        return emissor() + "/oauth2/token";
    }
}
