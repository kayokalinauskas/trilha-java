package dev.trilhajava.m13.aulas.microsservicos.estoque;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** A API pública do serviço de estoque: o único jeito de outro serviço chegar aos dados dele. */
@RestController
public class EstoqueController {

    public record PedidoDeReserva(String pedidoId, String sku, int quantidade) {}

    public record SaldoV1(String sku, int saldo) {}

    public record SaldoV2(String sku, int disponivel, int reservado) {}

    private final Estoque estoque;

    public EstoqueController(Estoque estoque) {
        this.estoque = estoque;
    }

    @PostMapping("/reservas")
    public Estoque.Reserva reservar(@RequestBody PedidoDeReserva pedido) throws InterruptedException {
        try {
            Estoque.Reserva reserva = estoque.reservar(pedido.pedidoId(), pedido.sku(), pedido.quantidade());
            Thread.sleep(estoque.lentidaoSimulada());        // reservou, mas demora a responder
            return reserva;
        } catch (Estoque.ReservaRecusada e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @DeleteMapping("/reservas/{pedidoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liberar(@PathVariable String pedidoId) {
        estoque.liberar(pedidoId);
    }

    @GetMapping(path = "/produtos/{sku}/saldo", version = "1")
    public SaldoV1 saldoV1(@PathVariable String sku) {
        return new SaldoV1(sku, estoque.disponivel(sku));
    }

    @GetMapping(path = "/produtos/{sku}/saldo", version = "2")
    public SaldoV2 saldoV2(@PathVariable String sku) {
        return new SaldoV2(sku, estoque.disponivel(sku), estoque.reservado(sku));
    }
}
