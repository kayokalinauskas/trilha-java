package dev.trilhajava.m05;

import java.util.Set;

public class CicloDeDependenciaException extends RuntimeException {

    public CicloDeDependenciaException(Set<String> envolvidos) {
        super("Ciclo de dependências envolvendo: " + envolvidos);
    }
}
