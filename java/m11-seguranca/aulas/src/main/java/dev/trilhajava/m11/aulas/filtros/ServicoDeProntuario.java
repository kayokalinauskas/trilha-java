package dev.trilhajava.m11.aulas.filtros;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Prontuário é dado sensível de saúde: só quem tem o papel MEDICO lê.
 * A regra fica no método, além da rota: vale para qualquer caminho que chegue aqui.
 */
@Service
public class ServicoDeProntuario {

    @PreAuthorize("hasRole('MEDICO')")
    public String resumo(long idConsulta) {
        return "Consulta " + idConsulta + ": retorno em 30 dias, sem alergias registradas.";
    }
}
