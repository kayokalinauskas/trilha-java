package dev.trilhajava.m11.aulas.autorizacao;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Map;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

/**
 * Tokens de teste: o jwt() do spring-security-test monta o JWT já validado (sem assinatura) e
 * passa pelo MESMO conversor de papéis da aplicação.
 */
final class Tokens {

    /** usuário → {clínica, papel}. Duas clínicas: "sol" e "lua". */
    static final Map<String, List<String>> USUARIOS = Map.of(
            "pac-ana", List.of("sol", "PACIENTE"),
            "pac-bruno", List.of("sol", "PACIENTE"),
            "med-carla", List.of("sol", "MEDICO"),
            "med-diego", List.of("sol", "MEDICO"),
            "atd-elisa", List.of("sol", "ATENDENTE"),
            "med-heitor", List.of("lua", "MEDICO"),
            "atd-iara", List.of("lua", "ATENDENTE"));

    static final String SEGREDO = "prontuarios.jwt.segredo=segredo-de-teste-com-mais-de-32-bytes!!";

    private Tokens() {}

    static JwtRequestPostProcessor como(String usuario) {
        List<String> dados = USUARIOS.get(usuario);
        return jwt()
                .jwt(token -> token.subject(usuario)
                        .claim("clinica", dados.get(0))
                        .claim("papeis", List.of(dados.get(1))))
                .authorities(SegurancaDosProntuarios.papeis());
    }
}
