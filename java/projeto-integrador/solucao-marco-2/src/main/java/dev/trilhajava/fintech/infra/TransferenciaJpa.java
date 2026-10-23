package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Transferencia;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransferenciaJpa extends JpaRepository<Transferencia, UUID> {

    Optional<Transferencia> findByChaveIdempotencia(String chaveIdempotencia);
}
