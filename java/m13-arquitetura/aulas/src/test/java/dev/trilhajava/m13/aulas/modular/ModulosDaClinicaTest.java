package dev.trilhajava.m13.aulas.modular;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulosDaClinicaTest {

    ApplicationModules modulos = ApplicationModules.of(ClinicaApplication.class);

    @Test
    void fronteirasRespeitadas() {
        modulos.forEach(System.out::println);
        // detectViolations() sempre executa; verify() roda uma vez por instância em cache.
        modulos.detectViolations().throwIfPresent();
    }

    @Test
    void documentacaoGerada() {
        new Documenter(modulos).writeDocumentation();

        assertThat(Path.of("target", "spring-modulith-docs", "components.puml")).satisfies(Files::exists);
    }
}
