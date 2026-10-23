package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Violação (nome): um controller que não se chama *Controller. */
@RestController
public class PacienteRecurso {

    @GetMapping("/pacientes/ping")
    public String ping() {
        return "ok";
    }
}
