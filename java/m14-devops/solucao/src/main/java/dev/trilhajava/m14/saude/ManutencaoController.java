package dev.trilhajava.m14.saude;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tira a instância do balanceador sem matá-la: readiness vai para REFUSING_TRAFFIC
 * (o Kubernetes para de mandar tráfego), liveness continua UP (ninguém reinicia o pod).
 * Numa aplicação real, este endpoint fica atrás de autenticação de administrador.
 */
@RestController
@RequestMapping("/admin/manutencao")
class ManutencaoController {

    record Pedido(boolean ativo) {}

    private final ApplicationEventPublisher eventos;

    ManutencaoController(ApplicationEventPublisher eventos) {
        this.eventos = eventos;
    }

    @PostMapping
    ResponseEntity<Void> alternar(@RequestBody Pedido pedido) {
        ReadinessState estado = pedido.ativo() ? ReadinessState.REFUSING_TRAFFIC : ReadinessState.ACCEPTING_TRAFFIC;
        AvailabilityChangeEvent.publish(eventos, this, estado);
        return ResponseEntity.noContent().build();
    }
}
