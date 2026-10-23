package dev.trilhajava.m10.frete;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Regiao {
    NORTE("AC", "AP", "AM", "PA", "RO", "RR", "TO"),
    NORDESTE("AL", "BA", "CE", "MA", "PB", "PE", "PI", "RN", "SE"),
    CENTRO_OESTE("DF", "GO", "MT", "MS"),
    SUDESTE("ES", "MG", "RJ", "SP"),
    SUL("PR", "RS", "SC");

    private static final Map<String, Regiao> POR_UF = Stream.of(values())
            .flatMap(r -> Stream.of(r.ufs).map(uf -> Map.entry(uf, r)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private final String[] ufs;

    Regiao(String... ufs) {
        this.ufs = ufs;
    }

    /** Região da UF; UF inexistente → IllegalArgumentException. */
    public static Regiao de(String uf) {
        Regiao regiao = uf == null ? null : POR_UF.get(uf);
        if (regiao == null) {
            throw new IllegalArgumentException("UF inválida: " + uf);
        }
        return regiao;
    }
}
