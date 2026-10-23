package dev.trilhajava.m11.aulas.owasp.erros;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.LongFunction;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Um endpoint comum. O que muda entre as versões é só o tratador de erros (@RestControllerAdvice). */
@RestController
public class PedidoController {

    private final LongFunction<BigDecimal> totalDoPedido;

    /** @param totalDoPedido busca o total no banco; nos testes, uma lambda que pode falhar */
    public PedidoController(LongFunction<BigDecimal> totalDoPedido) {
        this.totalDoPedido = totalDoPedido;
    }

    @GetMapping("/pedidos/{id}")
    public Map<String, Object> buscar(@PathVariable long id) {
        return Map.of("id", id, "total", totalDoPedido.apply(id));
    }
}
