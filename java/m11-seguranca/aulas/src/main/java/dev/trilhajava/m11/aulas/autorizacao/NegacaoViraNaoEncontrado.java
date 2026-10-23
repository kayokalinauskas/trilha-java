package dev.trilhajava.m11.aulas.autorizacao;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Quando o @PreAuthorize nega, em vez do 403 padrão lança a mesma exceção do "não existe" (404).
 * Assume que o primeiro argumento do método protegido é o id do prontuário.
 */
@Component
public class NegacaoViraNaoEncontrado implements MethodAuthorizationDeniedHandler {

    @Override
    public Object handleDeniedInvocation(MethodInvocation invocacao, AuthorizationResult resultado) {
        long prontuarioId = (Long) invocacao.getArguments()[0];
        throw new ProntuarioNaoEncontradoException(prontuarioId);
    }
}
