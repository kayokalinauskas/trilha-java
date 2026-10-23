package dev.trilhajava.m11.aulas.defesas;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cota de requisições da API: um balde por USUÁRIO autenticado (ou por IP, nas rotas públicas).
 * Roda dentro da cadeia do Spring Security, logo depois da autenticação, para saber quem está chamando.
 * Exportar um extrato custa 10 créditos; as demais rotas, 1.
 *
 * <p>Não é um {@code @Component} de propósito: o Spring Boot registraria um Filter que é bean também
 * no contêiner de servlets, e ele rodaria duas vezes (uma fora da segurança, sem saber o usuário).
 */
public class FiltroDeCota extends OncePerRequestFilter {

    static final int CUSTO_DA_EXPORTACAO = 10;
    static final URI TIPO = URI.create("https://api.contadigital.dev/problemas/cota-excedida");

    private final LimitadorDeCreditos limitador;
    private final JsonMapper json;

    public FiltroDeCota(LimitadorDeCreditos limitador, JsonMapper json) {
        this.limitador = limitador;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        return !requisicao.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        PoliticaDeLimite politica = limitador.politica();
        Consumo consumo = limitador.consumir(chaveDe(requisicao), custoDe(requisicao));

        // Draft IETF "RateLimit header fields for HTTP" (draft-ietf-httpapi-ratelimit-headers-11).
        resposta.setHeader("RateLimit-Policy", "\"%s\";q=%d;w=%d".formatted(
                politica.nome(), politica.capacidade(), politica.janela().toSeconds()));
        resposta.setHeader("RateLimit", "\"%s\";r=%d;t=%d".formatted(
                politica.nome(), consumo.restantes(), Consumo.emSegundos(consumo.ateEncher())));

        if (consumo.permitido()) {
            cadeia.doFilter(requisicao, resposta);
            return;
        }
        long segundos = Math.max(1, Consumo.emSegundos(consumo.tentarDepoisDe()));
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Cota de %d créditos por minuto esgotada. Tente de novo em %d s.".formatted(
                        politica.capacidade(), segundos));
        problema.setType(TIPO);
        problema.setTitle("Cota de requisições excedida");
        problema.setInstance(URI.create(requisicao.getRequestURI()));

        resposta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resposta.setHeader("Retry-After", String.valueOf(segundos));
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        json.writeValue(resposta.getOutputStream(), problema);
    }

    private static String chaveDe(HttpServletRequest requisicao) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.isAuthenticated()
                && !(autenticacao instanceof AnonymousAuthenticationToken)) {
            return "usuario:" + autenticacao.getName();
        }
        // Atrás de proxy, getRemoteAddr() só é o IP do cliente com server.forward-headers-strategy configurado.
        return "ip:" + requisicao.getRemoteAddr();
    }

    private static int custoDe(HttpServletRequest requisicao) {
        boolean exportacao = "POST".equals(requisicao.getMethod())
                && "/api/exportacoes".equals(requisicao.getRequestURI());
        return exportacao ? CUSTO_DA_EXPORTACAO : 1;
    }
}
