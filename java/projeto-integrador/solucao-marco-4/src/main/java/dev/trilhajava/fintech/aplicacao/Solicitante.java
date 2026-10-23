package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Conta;
import java.util.Objects;
import java.util.UUID;

/**
 * Quem está pedindo — extraído do token pela camada web. Os casos de uso recebem o solicitante
 * explicitamente: a regra "só o dono acessa" fica testável sem HTTP, sem Spring Security e sem mocks de contexto.
 */
public record Solicitante(UUID usuario, boolean admin) {

    public Solicitante {
        Objects.requireNonNull(usuario);
    }

    public static Solicitante cliente(UUID usuario) {
        return new Solicitante(usuario, false);
    }

    public static Solicitante administrador(UUID usuario) {
        return new Solicitante(usuario, true);
    }

    public boolean podeAcessar(Conta conta) {
        return admin || conta.pertenceA(usuario);
    }
}
