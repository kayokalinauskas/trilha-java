package dev.trilhajava.m11.delivery;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntregaController {

    @PostMapping("/entregas/{id}/concluir")
    public Map<String, Object> concluir(@PathVariable long id, @AuthenticationPrincipal Jwt token) {
        return Map.of("entrega", id, "concluidaPor", token.getSubject());
    }
}
