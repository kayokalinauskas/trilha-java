package dev.trilhajava.m06;

import java.util.Map;

/** Sucessos (id → número da nota) e falhas (id → mensagem), na ordem dos ids recebidos. Já pronto. */
public record ResultadoLote(Map<String, String> sucessos, Map<String, String> falhas) {
}
