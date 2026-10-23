package dev.trilhajava.fintech.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 401 e 403 nascem nos filtros de segurança, antes dos controllers: o TratadorDeErros não os vê.
 * Esta classe escreve essas respostas no mesmo formato Problem Details do resto da API.
 */
@Component
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final String BASE = "https://fintech.dev/problemas/";

    private final BearerTokenAuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();
    private final JsonMapper json;

    public RespostasDeSeguranca(JsonMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException erro)
            throws IOException {
        bearer.commence(requisicao, resposta, erro);   // status 401 + WWW-Authenticate: Bearer ...
        escrever(requisicao, resposta, 401, "nao-autenticado", "Não autenticado",
                "Envie um token válido no header Authorization: Bearer <token>. Obtenha um em POST /auth/token.");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException erro)
            throws IOException {
        escrever(requisicao, resposta, 403, "acesso-negado", "Acesso negado", "Você não tem permissão para esta operação.");
    }

    private void escrever(HttpServletRequest requisicao, HttpServletResponse resposta, int status, String tipo,
            String titulo, String detalhe) throws IOException {
        Map<String, Object> problema = new LinkedHashMap<>();
        problema.put("type", BASE + tipo);
        problema.put("title", titulo);
        problema.put("status", status);
        problema.put("detail", detalhe);
        problema.put("instance", requisicao.getRequestURI());
        resposta.setStatus(status);
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
