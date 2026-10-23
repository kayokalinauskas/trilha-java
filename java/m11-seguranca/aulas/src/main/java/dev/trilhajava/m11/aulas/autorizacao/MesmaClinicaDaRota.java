package dev.trilhajava.m11.aulas.autorizacao;

import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

/**
 * AuthorizationManager de ROTA: em /clinicas/{clinicaId}/**, a clínica do caminho precisa ser a do token.
 * É a checagem de tenant feita antes de qualquer controller rodar.
 */
@Component
public class MesmaClinicaDaRota implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> autenticacao,
            RequestAuthorizationContext contexto) {
        String clinicaDaRota = contexto.getVariables().get("clinicaId");
        if (clinicaDaRota != null && autenticacao.get() instanceof JwtAuthenticationToken token) {
            return new AuthorizationDecision(clinicaDaRota.equals(token.getToken().getClaimAsString("clinica")));
        }
        return new AuthorizationDecision(false);
    }
}
