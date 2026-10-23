package dev.trilhajava.m11.limite;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Limita o POST /login a 5 tentativas por minuto por IP — freia ataques de força bruta e
 * credential stuffing (OWASP A07). Um filtro de servlet roda antes do Spring MVC: a requisição
 * barrada nem chega ao controller.
 */
@Component
public class FiltroDeLimite extends OncePerRequestFilter {

    static final int TENTATIVAS = 5;
    static final Duration JANELA = Duration.ofMinutes(1);

    private final LimitadorPorChave limitador;
    private final JsonMapper json;

    public FiltroDeLimite(Clock relogio, JsonMapper json) {
        this.limitador = new LimitadorPorChave(TENTATIVAS, JANELA, relogio);
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        return !("POST".equals(requisicao.getMethod()) && "/login".equals(requisicao.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        // getRemoteAddr() é o IP da conexão. Atrás de um proxy/load balancer, ele é o IP do proxy:
        // configure server.forward-headers-strategy e confie no X-Forwarded-For SÓ se vier do seu proxy.
        Decisao decisao = limitador.tentar(requisicao.getRemoteAddr());
        resposta.setHeader("RateLimit-Limit", String.valueOf(TENTATIVAS));
        resposta.setHeader("RateLimit-Remaining", String.valueOf(decisao.restantes()));
        if (decisao.permitida()) {
            cadeia.doFilter(requisicao, resposta);
            return;
        }
        long segundos = Math.max(1, (decisao.tentarDepoisDe().toMillis() + 999) / 1000);   // arredonda para cima
        Map<String, Object> problema = new LinkedHashMap<>();
        problema.put("type", "https://api.exemplo.dev/problemas/muitas-requisicoes");
        problema.put("title", "Muitas requisições");
        problema.put("status", 429);
        problema.put("detail", "Limite de " + TENTATIVAS + " tentativas de login por minuto. Tente de novo em "
                + segundos + " s.");
        resposta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resposta.setHeader("Retry-After", String.valueOf(segundos));
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
