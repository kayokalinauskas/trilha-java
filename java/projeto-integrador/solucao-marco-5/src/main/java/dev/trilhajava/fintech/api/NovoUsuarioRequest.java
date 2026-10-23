package dev.trilhajava.fintech.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sem campo "papel": quem se cadastra é sempre CLIENTE (evita atribuição em massa de privilégio). */
public record NovoUsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 64) String senha) {

    @Override
    public String toString() {
        return "NovoUsuarioRequest[nome=" + nome + ", email=" + email + ", senha=***]";   // nunca logue a senha
    }
}
