package dev.trilhajava.fintech.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** A porta de persistência de contas — a mesma do marco 1, agora com lock para atualização. */
public interface RepositorioDeContas {

    void salvar(Conta conta);

    Optional<Conta> buscar(UUID id);

    /** Busca travando a linha até o fim da transação (SELECT ... FOR UPDATE). */
    Optional<Conta> buscarParaAtualizar(UUID id);

    boolean existe(Cpf cpf, TipoConta tipo);

    List<Conta> listar(Cpf cpf, int pagina, int tamanho);

    long contar(Cpf cpf);
}
