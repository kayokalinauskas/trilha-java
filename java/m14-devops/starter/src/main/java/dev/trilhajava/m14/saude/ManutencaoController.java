package dev.trilhajava.m14.saude;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /admin/manutencao {"ativo": true|false} liga e desliga o modo de manutenção. */
@RestController
@RequestMapping("/admin/manutencao")
class ManutencaoController {

    record Pedido(boolean ativo) {}

    @PostMapping
    ResponseEntity<Void> alternar(@RequestBody Pedido pedido) {
        // TODO: publique a mudança de estado de readiness e responda 204.
        throw new UnsupportedOperationException("TODO: exercício 14.03");
    }
}
