package dev.trilhajava.m11.aulas.filtros;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 401 e 403 nascem nos filtros, antes do DispatcherServlet: o @RestControllerAdvice não os vê.
 * Esta classe escreve as duas respostas no mesmo formato do resto da API (RFC 9457).
 */
@Component
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final String BASE = "https://clinica.exemplo.dev/problemas/";

    private final JacksonJsonHttpMessageConverter conversor;

    public RespostasDeSeguranca(JsonMapper json) {
        this.conversor = new JacksonJsonHttpMessageConverter(json);
    }

    /** 401: não há credencial, ou ela é inválida. O cliente pode tentar de novo, autenticando-se. */
    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException erro)
            throws IOException {
        resposta.setHeader("WWW-Authenticate", "Basic realm=\"clinica\"");
        escrever(requisicao, resposta, HttpStatus.UNAUTHORIZED, "nao-autenticado", "Não autenticado",
                "Envie credenciais válidas para acessar a API.");
    }

    /** 403: a credencial é válida, mas não dá direito a esta operação. Tentar de novo não adianta. */
    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException erro)
            throws IOException {
        escrever(requisicao, resposta, HttpStatus.FORBIDDEN, "acesso-negado", "Acesso negado",
                "Seu perfil não tem permissão para esta operação.");
    }

    private void escrever(HttpServletRequest requisicao, HttpServletResponse resposta, HttpStatus status,
            String tipo, String titulo, String detalhe) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create(BASE + tipo));
        problema.setTitle(titulo);
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        problema.setProperty("correlationId", requisicao.getAttribute(FiltroDeCorrelacao.ATRIBUTO));
        resposta.setStatus(status.value());
        conversor.write(problema, MediaType.APPLICATION_PROBLEM_JSON, new ServletServerHttpResponse(resposta));
    }
}
