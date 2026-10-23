package dev.trilhajava.m13.aulas.hexagonal.adaptador.web;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ComandoDeResgate;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ResgatarRecompensa;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Resgate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada: traduz HTTP para a porta de entrada e o resultado de volta para JSON. */
@RestController
@RequestMapping("/clientes/{clienteId}/resgates")
class ResgateController {

    private final ResgatarRecompensa resgatarRecompensa;   // a porta, não o serviço concreto

    ResgateController(ResgatarRecompensa resgatarRecompensa) {
        this.resgatarRecompensa = resgatarRecompensa;
    }

    @PostMapping
    ResponseEntity<RespostaDeResgate> resgatar(@PathVariable String clienteId,
                                               @Valid @RequestBody PedidoDeResgate pedido) {
        Resgate resgate = resgatarRecompensa.resgatar(new ComandoDeResgate(clienteId, pedido.recompensa()));
        URI local = URI.create("/clientes/" + clienteId + "/resgates/" + resgate.id());
        return ResponseEntity.created(local).body(RespostaDeResgate.de(resgate));
    }

    /** O JSON que chega. Validação de formato fica na borda; regra de negócio, no núcleo. */
    record PedidoDeResgate(@NotBlank String recompensa) {
    }

    /** O JSON que sai: o contrato da API, separado do modelo de domínio. */
    record RespostaDeResgate(String id, String recompensa, long pontosDebitados,
                             long saldoRestante, Instant realizadoEm) {

        static RespostaDeResgate de(Resgate resgate) {
            return new RespostaDeResgate(resgate.id(), resgate.recompensa(),
                    resgate.debitados().quantidade(), resgate.saldoRestante().quantidade(),
                    resgate.realizadoEm());
        }
    }
}
