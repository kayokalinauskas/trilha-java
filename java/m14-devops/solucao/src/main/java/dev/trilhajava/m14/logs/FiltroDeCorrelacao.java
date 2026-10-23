package dev.trilhajava.m14.logs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Dá a cada requisição um id de correlação: o que veio no header X-Request-Id (se for válido)
 * ou um novo. O id vai para o MDC (e daí para TODOS os logs da requisição) e para a resposta.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroDeCorrelacao extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String CHAVE_MDC = "requestId";

    /** Só letras, números, ponto, hífen e sublinhado, até 64: nada de quebra de linha forjando logs. */
    private static final Pattern VALIDO = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String recebido = requisicao.getHeader(HEADER);
        String id = recebido != null && VALIDO.matcher(recebido).matches() ? recebido : UUID.randomUUID().toString();
        MDC.put(CHAVE_MDC, id);
        resposta.setHeader(HEADER, id);
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            // A thread volta para o pool do Tomcat: sem isto, a próxima requisição herdaria o id.
            MDC.remove(CHAVE_MDC);
        }
    }
}
