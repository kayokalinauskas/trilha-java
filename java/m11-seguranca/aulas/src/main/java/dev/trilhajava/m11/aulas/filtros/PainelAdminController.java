package dev.trilhajava.m11.aulas.filtros;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** O console administrativo: acessado pelo navegador, com sessão e login por formulário. */
@RestController
public class PainelAdminController {

    @GetMapping("/admin/painel")
    String painel(Authentication quem) {
        return "Painel da clínica. Olá, " + quem.getName() + ".";
    }

    @PostMapping("/admin/cache/limpar")
    String limparCache() {
        return "Cache de agenda limpo.";
    }
}
