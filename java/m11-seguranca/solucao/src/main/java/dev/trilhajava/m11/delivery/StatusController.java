package dev.trilhajava.m11.delivery;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/publico/status")
    public Map<String, String> status() {
        return Map.of("status", "ok");
    }
}
