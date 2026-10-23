package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.Notificacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

interface NotificacaoJpa extends JpaRepository<Notificacao, UUID> {

    List<Notificacao> findByUsuarioOrderByCriadaEmDesc(UUID usuario, Limit limite);
}
