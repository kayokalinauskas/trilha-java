package dev.trilhajava.m11.aulas.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registra quem acessou o quê e com que resultado, na cadeia da API.
 *
 * <p>Ele também põe o usuário no MDC, para que todo log da requisição (controller, serviço)
 * diga quem a fez. A posição importa: ele precisa ficar DEPOIS dos filtros de autenticação
 * (para saber quem é) e ANTES do ExceptionTranslationFilter (para ver o 401/403 que esse
 * filtro escreve).
 *
 * <p>Não é um bean de propósito: um Filter anotado com @Component seria registrado também
 * pelo Spring Boot e rodaria duas vezes.
 */
public class FiltroDeAuditoria extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroDeAuditoria.class);

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String usuario = usuarioAtual();
        MDC.put("usuario", usuario);
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            log.info("auditoria usuario={} metodo={} caminho={} status={}",
                    usuario, requisicao.getMethod(), requisicao.getRequestURI(), resposta.getStatus());
            MDC.remove("usuario");
        }
    }

    private static String usuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return autenticacao == null ? "(nenhum)" : autenticacao.getName();
    }
}
