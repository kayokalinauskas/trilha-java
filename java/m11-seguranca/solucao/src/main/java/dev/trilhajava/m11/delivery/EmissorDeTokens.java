package dev.trilhajava.m11.delivery;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class EmissorDeTokens {

    public record TokenEmitido(String accessToken, String tokenType, long expiresIn) {
    }

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;
    private final Clock clock = Clock.systemUTC();

    public EmissorDeTokens(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.propriedades = propriedades;
    }

    public TokenEmitido emitir(Authentication autenticado) {
        Instant agora = clock.instant();
        // Só os papéis (ROLE_*): o Spring Security 7 também adiciona autoridades de fator,
        // como FACTOR_PASSWORD, que não devem virar papéis no token.
        List<String> papeis = autenticado.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridade -> autoridade.startsWith("ROLE_"))
                .map(papel -> papel.substring("ROLE_".length()))
                .sorted()
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .subject(autenticado.getName())
                .issuedAt(agora)
                .expiresAt(agora.plus(propriedades.validade()))
                .claim("roles", papeis)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(token, "Bearer", propriedades.validade().toSeconds());
    }
}
