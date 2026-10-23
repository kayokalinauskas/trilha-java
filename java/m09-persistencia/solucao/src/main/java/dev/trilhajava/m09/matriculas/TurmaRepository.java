package dev.trilhajava.m09.matriculas;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    Optional<Turma> findByCodigo(String codigo);

    /**
     * SELECT ... FOR UPDATE: trava a linha da turma até o fim da transação. Duas matrículas
     * simultâneas na mesma turma passam a acontecer uma depois da outra — a contagem de vagas
     * de uma já enxerga a matrícula da outra.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.id = :id")
    Optional<Turma> buscarParaAtualizar(long id);
}
