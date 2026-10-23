package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import dev.trilhajava.fintech.dominio.TipoConta;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

/** Detalhe de infraestrutura: o domínio não conhece o Spring Data, só a porta RepositorioDeContas. */
interface ContaJpa extends JpaRepository<Conta, UUID> {

    boolean existsByCpfAndTipo(Cpf cpf, TipoConta tipo);

    Page<Conta> findByCpf(Cpf cpf, Pageable pageable);

    long countByCpf(Cpf cpf);

    Page<Conta> findByDono(UUID dono, Pageable pageable);

    long countByDono(UUID dono);

    Page<Conta> findByCpfAndDono(Cpf cpf, UUID dono, Pageable pageable);

    long countByCpfAndDono(Cpf cpf, UUID dono);

    boolean existsByIdAndDono(UUID id, UUID dono);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> buscarComLock(UUID id);
}
