package dev.trilhajava.m13.aulas.decisoes.frete;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Flags num mapa: suficiente para testes e para mostrar a ideia. Flag desconhecida vale 0 (desligada). */
public final class FlagsEmMemoria implements FeatureFlags {

    private final Map<String, Integer> percentuais = new ConcurrentHashMap<>();

    public void definir(String flag, int percentual) {
        if (percentual < 0 || percentual > 100) {
            throw new IllegalArgumentException("percentual fora de 0..100: " + percentual);
        }
        percentuais.put(flag, percentual);
    }

    @Override
    public int percentualAtivo(String flag) {
        return percentuais.getOrDefault(flag, 0);
    }
}
