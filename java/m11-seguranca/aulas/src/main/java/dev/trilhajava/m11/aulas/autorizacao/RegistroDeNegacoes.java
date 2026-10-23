package dev.trilhajava.m11.aulas.autorizacao;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

/**
 * Auditoria das negações: QUEM tentou O QUÊ em QUAL recurso e o resultado. Nunca o conteúdo do prontuário.
 * Recebe as negações decididas no código (404/403 do serviço) e as do Spring Security (eventos).
 */
@Component
public class RegistroDeNegacoes {

    private static final Logger log = LoggerFactory.getLogger("auditoria.autorizacao");

    private final List<String> recentes = new ArrayList<>();

    public void registrar(Solicitante quem, String operacao, String recurso, String resultado) {
        registrar(quem.usuario(), quem.clinicaId(), operacao, recurso, resultado);
    }

    private void registrar(String usuario, String clinicaId, String operacao, String recurso, String resultado) {
        String linha = "negado usuario=%s clinica=%s operacao=%s recurso=%s resultado=%s"
                .formatted(usuario, clinicaId, operacao, recurso, resultado);
        log.warn(linha);
        synchronized (recentes) {
            recentes.add(linha);
        }
    }

    /** Negações decididas pelo próprio Spring Security (na rota ou num @PreAuthorize). */
    @EventListener
    public void aoNegar(AuthorizationDeniedEvent<?> evento) {
        String usuario = evento.getAuthentication().get().getName();
        String alvo = switch (evento.getObject()) {
            case MethodInvocation metodo -> "metodo/" + metodo.getMethod().getName();
            case HttpServletRequest requisicao -> "rota" + requisicao.getRequestURI();
            default -> evento.getObject().getClass().getSimpleName();
        };
        registrar(usuario, "?", "spring-security", alvo, "NEGADO");
    }

    public List<String> recentes() {
        synchronized (recentes) {
            return List.copyOf(recentes);
        }
    }

    public void limpar() {
        synchronized (recentes) {
            recentes.clear();
        }
    }
}
