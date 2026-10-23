package dev.trilhajava.m14.logs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Dá a cada requisição um id de correlação (header X-Request-Id).
 */
@Component
public class FiltroDeCorrelacao extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String CHAVE_MDC = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        // TODO: use o X-Request-Id recebido se for válido (letras, números, ponto, hífen e sublinhado,
        //  até 64); senão, gere um UUID. Coloque no MDC e no header da resposta; limpe o MDC no fim.
        //  Este filtro deve rodar antes de todos os outros.
        cadeia.doFilter(requisicao, resposta);
    }
}
