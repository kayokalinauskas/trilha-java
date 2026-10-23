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

    /**
     * A conta existe e é deste usuário? Uma consulta "exists", que NÃO carrega a entidade: carregar a conta
     * antes do SELECT ... FOR UPDATE deixaria uma cópia velha no contexto de persistência.
     */
    boolean pertenceA(UUID conta, UUID usuario);

    /** {@code cpf} e {@code dono} são filtros opcionais (null = sem filtro). */
    List<Conta> listar(Cpf cpf, UUID dono, int pagina, int tamanho);

    long contar(Cpf cpf, UUID dono);
}
