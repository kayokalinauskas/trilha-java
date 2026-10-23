package dev.trilhajava.fintech.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A PORTA de persistência, definida pelo domínio. A implementação fica em infra:
 * em memória no marco 1, JPA no marco 2 — sem mudar nada no domínio nem nos casos de uso.
 */
public interface RepositorioDeContas {

    void salvar(Conta conta);

    Optional<Conta> buscar(UUID id);

    boolean existe(Cpf cpf, TipoConta tipo);

    /** Contas ordenadas por data de abertura; cpf nulo = todas. */
    List<Conta> listar(Cpf cpf, int pagina, int tamanho);

    long contar(Cpf cpf);
}
