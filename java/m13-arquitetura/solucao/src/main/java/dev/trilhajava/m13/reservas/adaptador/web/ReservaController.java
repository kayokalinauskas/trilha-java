package dev.trilhajava.m13.reservas.adaptador.web;

import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.CancelarReserva;
import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.ConsultarAgenda;
import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.ReservarSala;
import dev.trilhajava.m13.reservas.aplicacao.porta.entrada.ReservarSala.PedidoDeReserva;
import dev.trilhajava.m13.reservas.dominio.Reserva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Adaptador de ENTRADA: traduz HTTP/JSON para as portas de entrada, e o resultado de volta.
 * Depende só das interfaces dos casos de uso — nunca do ServicoDeReservas.
 */
@RestController
class ReservaController {

    record NovaReserva(@NotBlank String sala, int participantes, @NotNull LocalDateTime inicio,
            @NotNull LocalDateTime fim) {
    }

    record ReservaResponse(UUID id, String sala, String responsavel, int participantes, LocalDateTime inicio,
            LocalDateTime fim, String status) {

        static ReservaResponse de(Reserva r) {
            return new ReservaResponse(r.id(), r.sala(), r.responsavel(), r.participantes(), r.periodo().inicio(),
                    r.periodo().fim(), r.status().name());
        }
    }

    private final ReservarSala reservar;
    private final CancelarReserva cancelar;
    private final ConsultarAgenda consultar;

    ReservaController(ReservarSala reservar, CancelarReserva cancelar, ConsultarAgenda consultar) {
        this.reservar = reservar;
        this.cancelar = cancelar;
        this.consultar = consultar;
    }

    @PostMapping("/reservas")
    ResponseEntity<ReservaResponse> reservar(@RequestHeader("X-Usuario") String usuario,
            @Valid @RequestBody NovaReserva corpo, UriComponentsBuilder uri) {
        Reserva reserva = reservar.reservar(
                new PedidoDeReserva(corpo.sala(), usuario, corpo.participantes(), corpo.inicio(), corpo.fim()));
        return ResponseEntity.created(uri.path("/reservas/{id}").buildAndExpand(reserva.id()).toUri())
                .body(ReservaResponse.de(reserva));
    }

    @DeleteMapping("/reservas/{id}")
    ResponseEntity<Void> cancelar(@RequestHeader("X-Usuario") String usuario, @PathVariable UUID id) {
        cancelar.cancelar(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/salas/{sala}/agenda")
    List<ReservaResponse> agenda(@PathVariable String sala,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
        return consultar.agenda(sala, dia).stream().map(ReservaResponse::de).toList();
    }
}
