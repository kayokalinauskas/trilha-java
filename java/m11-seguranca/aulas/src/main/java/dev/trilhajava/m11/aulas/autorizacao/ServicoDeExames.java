package dev.trilhajava.m11.aulas.autorizacao;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.stereotype.Service;

/**
 * A mesma regra de objeto, agora declarada com method security: um bean (@regras) dentro do SpEL.
 * Sem o @HandleAuthorizationDenied, a negação viraria 403 e confirmaria que o prontuário existe.
 */
@Service
public class ServicoDeExames {

    private final RepositorioDeProntuarios repositorio;

    public ServicoDeExames(RepositorioDeProntuarios repositorio) {
        this.repositorio = repositorio;
    }

    @PreAuthorize("@regras.podeVerExames(authentication, #prontuarioId)")
    @HandleAuthorizationDenied(handlerClass = NegacaoViraNaoEncontrado.class)
    public List<String> exames(long prontuarioId) {
        return repositorio.examesDo(prontuarioId);
    }
}
