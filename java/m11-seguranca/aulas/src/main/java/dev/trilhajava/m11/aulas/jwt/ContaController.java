package dev.trilhajava.m11.aulas.jwt;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rotas de exemplo. A regra de acesso de cada uma está em {@link SegurancaDaApi}. */
@RestController
public class ContaController {

    public record Perfil(String cliente, String clienteApp, List<String> autoridades) {}

    public record Extrato(String cliente, BigDecimal saldo) {}

    public record PixAceito(String cliente, String situacao) {}

    /** Qualquer token válido: mostra o que o Spring extraiu dele. */
    @GetMapping("/eu")
    Perfil eu(JwtAuthenticationToken autenticacao) {
        List<String> autoridades = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return new Perfil(autenticacao.getName(), autenticacao.getToken().getClaimAsString("azp"), autoridades);
    }

    @GetMapping("/contas/minha/extrato")
    Extrato extrato(JwtAuthenticationToken autenticacao) {
        return new Extrato(autenticacao.getName(), new BigDecimal("1530.42"));
    }

    @PostMapping("/contas/minha/pix")
    PixAceito pix(JwtAuthenticationToken autenticacao) {
        return new PixAceito(autenticacao.getName(), "EM_PROCESSAMENTO");
    }

    @GetMapping("/backoffice/contas")
    List<String> contasParaOSuporte() {
        return List.of("conta-1001", "conta-1002");
    }
}
