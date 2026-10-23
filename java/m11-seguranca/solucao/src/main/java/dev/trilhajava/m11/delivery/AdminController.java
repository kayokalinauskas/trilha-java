package dev.trilhajava.m11.delivery;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/admin/relatorio")
    public Map<String, Integer> relatorio() {
        return Map.of("pedidosHoje", 42);
    }
}
