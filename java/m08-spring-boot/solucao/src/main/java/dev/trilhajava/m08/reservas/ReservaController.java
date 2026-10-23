package dev.trilhajava.m08.reservas;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Repare: nenhum try/catch aqui. Os erros sobem e são traduzidos em UM lugar — o TratadorDeErros. */
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ServicoDeReservas servico;

    public ReservaController(ServicoDeReservas servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<Reserva> reservar(@Valid @RequestBody NovaReserva pedido, UriComponentsBuilder uri) {
        Reserva reserva = servico.reservar(pedido);
        return ResponseEntity.created(uri.path("/reservas/{id}").buildAndExpand(reserva.id()).toUri()).body(reserva);
    }

    @GetMapping("/{id}")
    public Reserva buscar(@PathVariable long id) {
        return servico.buscar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable long id) {
        servico.cancelar(id);
    }
}
