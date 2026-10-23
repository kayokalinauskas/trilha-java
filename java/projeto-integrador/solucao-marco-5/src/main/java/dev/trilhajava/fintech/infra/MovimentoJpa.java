package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Movimento;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface MovimentoJpa extends JpaRepository<Movimento, Long> {

    @Query("select m from Movimento m where m.conta.id = :contaId order by m.id")
    List<Movimento> extrato(UUID contaId);
}
