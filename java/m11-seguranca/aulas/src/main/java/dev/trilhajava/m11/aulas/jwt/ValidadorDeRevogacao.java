package dev.trilhajava.m11.aulas.jwt;

import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Checkpoint da aula 11.03: recusa tokens cujo "jti" está numa lista de revogados (denylist).
 * Token sem "jti" também é recusado: sem identificador, não haveria como revogá-lo.
 */
public final class ValidadorDeRevogacao implements OAuth2TokenValidator<Jwt> {

    private final Predicate<String> estaRevogado;

    /** @param estaRevogado consulta a denylist (em produção, um Redis com TTL igual ao exp restante) */
    public ValidadorDeRevogacao(Predicate<String> estaRevogado) {
        this.estaRevogado = Objects.requireNonNull(estaRevogado);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId();
        if (jti == null || jti.isBlank()) {
            return falha("The jti claim is required");
        }
        if (estaRevogado.test(jti)) {
            return falha("The token has been revoked");
        }
        return OAuth2TokenValidatorResult.success();
    }

    private static OAuth2TokenValidatorResult falha(String descricao) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, descricao, null));
    }
}
