package dev.trilhajava.m11.aulas.oidc.api;

import java.math.BigDecimal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PedidoController {

    record ResumoDoPedido(String numero, String status, BigDecimal total, String chamadoPor) {
    }

    /**
     * Dados fixos: o foco aqui é quem pode chamar, não o pedido. O sub do token é o
     * client_id (client credentials) ou a pessoa (authorization code).
     */
    @GetMapping("/pedidos/{numero}")
    ResumoDoPedido buscar(@PathVariable String numero, @AuthenticationPrincipal Jwt token) {
        return new ResumoDoPedido(numero, "PAGO", new BigDecimal("259.90"), token.getSubject());
    }
}
