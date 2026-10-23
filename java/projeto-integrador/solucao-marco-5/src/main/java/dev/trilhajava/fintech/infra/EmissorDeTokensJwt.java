package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.aplicacao.EmissorDeTokens;
import dev.trilhajava.fintech.aplicacao.TokenEmitido;
import dev.trilhajava.fintech.dominio.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** O adaptador da porta EmissorDeTokens: JWT HS256 com sub = id do usuário e roles = [papel]. */
@Component
class EmissorDeTokensJwt implements EmissorDeTokens {

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;
    private final Clock clock;

    EmissorDeTokensJwt(JwtEncoder encoder, JwtProperties propriedades, Clock clock) {
        this.encoder = encoder;
        this.propriedades = propriedades;
        this.clock = clock;
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .subject(usuario.id().toString())       // um id estável e opaco — não o e-mail
                .issuedAt(agora)
                .expiresAt(agora.plus(propriedades.validade()))
                .claim("roles", List.of(usuario.papel().name()))
                .build();
        var cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenEmitido(token, propriedades.validade());
    }
}
