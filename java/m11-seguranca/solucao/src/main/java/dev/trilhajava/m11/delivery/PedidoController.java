package dev.trilhajava.m11.delivery;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PedidoController {

    private static final Map<String, List<String>> PEDIDOS = Map.of(
            "ana", List.of("PED-1001", "PED-1002"),
            "carla", List.of("PED-1009"));

    /** O usuário vem do TOKEN (claim "sub"), nunca de um parâmetro que o cliente poderia trocar. */
    @GetMapping("/pedidos/meus")
    public Map<String, Object> meusPedidos(@AuthenticationPrincipal Jwt token) {
        return Map.of("usuario", token.getSubject(), "pedidos", PEDIDOS.getOrDefault(token.getSubject(), List.of()));
    }
}
