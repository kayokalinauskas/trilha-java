package dev.trilhajava.m11.aulas.dados;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByCpfIndice(String cpfIndice);
}
