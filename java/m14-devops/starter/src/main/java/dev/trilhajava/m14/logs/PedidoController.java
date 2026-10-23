package dev.trilhajava.m14.logs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Uma API mínima para gerar logs. Fornecida pronta. */
@RestController
@RequestMapping("/pedidos")
class PedidoController {

    record NovoPedido(@NotBlank String cliente, @NotBlank String cpf, @NotNull @DecimalMin("0.01") BigDecimal valor) {
    }

    private final ServicoDePedidos servico;

    PedidoController(ServicoDePedidos servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServicoDePedidos.Pedido criar(@Valid @RequestBody NovoPedido pedido) {
        return servico.criar(pedido.cliente(), pedido.cpf(), pedido.valor());
    }

    @GetMapping("/{id}")
    ServicoDePedidos.Pedido buscar(@PathVariable UUID id) {
        return servico.buscar(id);
    }
}
