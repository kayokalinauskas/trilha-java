package dev.trilhajava.fintech.infra.observabilidade;

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
 * X-Request-Id: mantém o recebido (se for seguro), senão gera um UUID. Vai para o MDC (todos os logs da
 * requisição) e para a resposta. Roda antes do filtro do Spring Security, então até um 401 leva o id.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroDeCorrelacao extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String CHAVE_MDC = "requestId";

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
            MDC.remove(CHAVE_MDC);
        }
    }
}
