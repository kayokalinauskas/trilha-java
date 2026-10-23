package dev.trilhajava.m11.aulas.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Dá a cada requisição um identificador de correlação (header X-Correlation-Id).
 *
 * <p>É um filtro de servlet comum, registrado pelo Spring Boot ANTES do filtro do Spring
 * Security (que tem ordem -100). Assim, até um 401 gerado dentro da cadeia de segurança
 * sai com o identificador, e os logs dos filtros de segurança já o têm no MDC.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FiltroDeCorrelacao extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String ATRIBUTO = FiltroDeCorrelacao.class.getName();

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String id = valido(requisicao.getHeader(HEADER)) ? requisicao.getHeader(HEADER) : UUID.randomUUID().toString();
        requisicao.setAttribute(ATRIBUTO, id);
        resposta.setHeader(HEADER, id);
        MDC.put("correlationId", id);
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            MDC.remove("correlationId");
        }
    }

    /** Aceita o identificador do cliente só se for curto e sem caracteres estranhos (evita injeção em log). */
    private static boolean valido(String id) {
        if (id == null || id.isEmpty() || id.length() > 64) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-') {
                return false;
            }
        }
        return true;
    }
}
