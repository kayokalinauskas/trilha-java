package dev.trilhajava.m11.limite;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Um login de mentira: o que importa aqui é quantas vezes ele pode ser chamado. */
@RestController
public class LoginController {

    @PostMapping("/login")
    public Map<String, Object> login() {
        return Map.of("ok", true);
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "no ar");
    }
}
