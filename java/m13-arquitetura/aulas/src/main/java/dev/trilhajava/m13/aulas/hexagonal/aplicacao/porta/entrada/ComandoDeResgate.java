package dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada;

/** Os dados de que o caso de uso precisa, nos termos do núcleo (sem JSON, sem anotações de validação). */
public record ComandoDeResgate(String clienteId, String codigoRecompensa) {

    public ComandoDeResgate {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("clienteId é obrigatório");
        }
        if (codigoRecompensa == null || codigoRecompensa.isBlank()) {
            throw new IllegalArgumentException("codigoRecompensa é obrigatório");
        }
    }
}
