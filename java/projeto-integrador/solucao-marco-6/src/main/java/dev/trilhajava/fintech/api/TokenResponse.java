package dev.trilhajava.fintech.api;

/** Nomes no padrão do OAuth 2.0 (RFC 6749), que clientes e ferramentas já conhecem. */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
